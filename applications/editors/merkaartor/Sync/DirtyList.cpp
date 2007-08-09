#include "Sync/DirtyList.h"
#include "Command/Command.h"
#include "Map/Coord.h"
#include "Map/DownloadOSM.h"
#include "Map/ExportOSM.h"
#include "Map/MapDocument.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"

#include <QtCore/QEventLoop>
#include <QtGui/QDialog>
#include <QtGui/QListWidget>
#include <QtGui/QMessageBox>
#include <QtGui/QProgressDialog>
#include <QtNetwork/QHttp>
#include <QtNetwork/QTcpSocket>

#include <algorithm>

static QString stripToOSMId(const QString& id)
{
	int f = id.lastIndexOf("_");
	if (f>0)
		return id.right(id.length()-(f+1));
	return id;
}

static QString userName(const MapFeature* F)
{
	QString s(F->tagValue("name",""));
	if (!s.isEmpty())
		return " ("+s+")";
	return "";
}

static bool isInterestingPoint(MapDocument* theDocument, TrackPoint* Pt)
{
	// does its id look like one from osm
	if (Pt->id().left(5) == "node_")
		return true;
	// if the user has added special tags, that fine also
	for (unsigned int i=0; i<Pt->tagSize(); ++i)
		if (Pt->tagKey(i) != "created_by")
			return true;
	// if it is part of a road, then too
	for (unsigned int j=0; j<theDocument->numLayers(); ++j)
	{
		MapLayer* theLayer = theDocument->layer(j);
		for (unsigned i=0; i<theLayer->size(); ++i)
		{
			Way* W = dynamic_cast<Way*>(theLayer->get(i));
			if (W)
			{
				if ( (W->from() == Pt) || (W->to() ==Pt) || (W->controlFrom() == Pt) || (W->controlTo() == Pt) )
					return true;
			}
		}
	}
	return false;
}

DirtyList::~DirtyList()
{
}

bool DirtyListBuild::add(MapFeature* F)
{
	Added.push_back(F);
	return false;
}

bool DirtyListBuild::update(MapFeature* F)
{
	for (unsigned int i=0; i<Updated.size(); ++i)
		if (Updated[i] == F)
		{
			UpdateCounter[i].first++;
			return false;
		}
	Updated.push_back(F);
	UpdateCounter.push_back(std::make_pair(1,0));
	return false;
}

bool DirtyListBuild::erase(MapFeature* F)
{
	Deleted.push_back(F);
	return false;
}

bool DirtyListBuild::willBeAdded(MapFeature* F) const
{
	return std::find(Added.begin(),Added.end(),F) != Added.end();
}

bool DirtyListBuild::willBeErased(MapFeature* F) const
{
	return std::find(Deleted.begin(),Deleted.end(),F) != Deleted.end();
}

bool DirtyListBuild::updateNow(MapFeature* F) const
{
	for (unsigned int i=0; i<Updated.size(); ++i)
		if (Updated[i] == F)
		{
			UpdateCounter[i].second++;
			return UpdateCounter[i].first == UpdateCounter[i].second;
		}
	return false;
}

void DirtyListBuild::resetUpdates()
{
	for (unsigned int i=0; i<UpdateCounter.size(); ++i)
		UpdateCounter[i].second = 0;
}

/* DIRTYLISTVISIT */

DirtyListVisit::DirtyListVisit(MapDocument* aDoc, const DirtyListBuild &aBuilder, bool b)
: theDocument(aDoc), Future(aBuilder), EraseFromHistory(b)
{
}

MapDocument* DirtyListVisit::document()
{
	return theDocument;
}

bool DirtyListVisit::notYetAdded(MapFeature* F)
{
	return std::find(AlreadyAdded.begin(),AlreadyAdded.end(),F) == AlreadyAdded.end();
}

bool DirtyListVisit::add(MapFeature* F)
{
	if (Future.willBeErased(F))
		return EraseFromHistory;
	for (unsigned int i=0; i<AlreadyAdded.size(); ++i)
		if (AlreadyAdded[i] == F)
			return EraseResponse[i];
	if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(F))
	{
		if (isInterestingPoint(theDocument,Pt))
		{
			bool x = addPoint(Pt);
			AlreadyAdded.push_back(F);
			EraseResponse.push_back(x);
			return x;
		}
		else
			return EraseFromHistory;
	}
	else if (Way* W = dynamic_cast<Way*>(F))
	{
		if (Future.willBeAdded(W->from()) && notYetAdded(W->from()))
			add(W->from());
		if (Future.willBeAdded(W->to()) && notYetAdded(W->to()))
			add(W->to());
		bool x = addWay(W);
		AlreadyAdded.push_back(F);
		EraseResponse.push_back(x);
		return x;
	}
	else if (Road* R = dynamic_cast<Road*>(F))
	{
		for (unsigned int i=0; i<R->size(); ++i)
			if (Future.willBeAdded(R->get(i)) && notYetAdded(R->get(i)))
				add(R->get(i));
		bool x = addRoad(R);
		AlreadyAdded.push_back(F);
		EraseResponse.push_back(x);
		return x;
	}
	return EraseFromHistory;
}

bool DirtyListVisit::update(MapFeature* F)
{
	if (Future.willBeErased(F) || Future.willBeAdded(F))
		return EraseFromHistory;
	if (!Future.updateNow(F))
		return EraseFromHistory;
	if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(F))
	{
		if (isInterestingPoint(theDocument,Pt))
			return updatePoint(Pt);
		else
			return EraseFromHistory;
	}
	else if (Way* W = dynamic_cast<Way*>(F))
		return updateWay(W);
	else if (Road* R = dynamic_cast<Road*>(F))
		return updateRoad(R);
	return EraseFromHistory;
}

bool DirtyListVisit::erase(MapFeature* F)
{
	if (Future.willBeAdded(F))
		return EraseFromHistory;
	if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(F))
	{
		if (isInterestingPoint(theDocument,Pt))
			return erasePoint(Pt);
		else
			return EraseFromHistory;
	}
	else if (Way* W = dynamic_cast<Way*>(F))
		return eraseWay(W);
	else if (Road* R = dynamic_cast<Road*>(F))
		return eraseRoad(R);
	return EraseFromHistory;
}

/* DIRTYLISTDESCRIBER */


DirtyListDescriber::DirtyListDescriber(MapDocument* aDoc, const DirtyListBuild& aFuture)
: DirtyListVisit(aDoc, aFuture, false), Task(0)
{
}

unsigned int DirtyListDescriber::tasks() const
{
	return Task;
}

bool DirtyListDescriber::showChanges(QWidget* aParent)
{
	QDialog* dlg = new QDialog(aParent);
	Ui.setupUi(dlg);

	document()->history().buildDirtyList(*this);

	bool ok = (dlg->exec() == QDialog::Accepted);

	Task = Ui.ChangesList->count();
	delete dlg;
	return ok;
}


bool DirtyListDescriber::addWay(Way* W)
{
	if (W->controlFrom() || W->controlTo())
		Ui.ChangesList->addItem(QString("IGNORE bezier link %1").arg(W->id()) + userName(W));
	else
		Ui.ChangesList->addItem(QString("ADD link %1").arg(W->id()));
	return false;
}

bool DirtyListDescriber::addRoad(Road* R)
{
	Ui.ChangesList->addItem(QString("ADD road %1").arg(R->id()) + userName(R));
	return false;
}

bool DirtyListDescriber::addPoint(TrackPoint* Pt)
{
	Ui.ChangesList->addItem(QString("ADD trackpoint %1").arg(Pt->id()) + userName(Pt));
	return false;
}

bool DirtyListDescriber::updateWay(Way* W)
{
	Ui.ChangesList->addItem(QString("UPDATE link %1").arg(W->id()) + userName(W));
	return false;
}

bool DirtyListDescriber::updatePoint(TrackPoint* Pt)
{
	Ui.ChangesList->addItem(QString("UPDATE trackpoint %1").arg(Pt->id()) + userName(Pt));
	return false;
}

bool DirtyListDescriber::updateRoad(Road* R)
{
	Ui.ChangesList->addItem(QString("UPDATE road %1").arg(R->id()) + userName(R));
	return false;
}

bool DirtyListDescriber::eraseWay(Way* W)
{
	Ui.ChangesList->addItem(QString("REMOVE link %1").arg(W->id()) + userName(W));
	return false;
}

bool DirtyListDescriber::erasePoint(TrackPoint* Pt)
{
	Ui.ChangesList->addItem(QString("REMOVE trackpoint %1").arg(Pt->id()) + userName(Pt));
	return false;
}

bool DirtyListDescriber::eraseRoad(Road* R)
{
	Ui.ChangesList->addItem(QString("REMOVE road %1").arg(R->id()) + userName(R));
	return false;
}


/* DIRTYLIST */


DirtyListExecutor::DirtyListExecutor(MapDocument* aDoc, const DirtyListBuild& aFuture, const QString& aWeb, const QString& aUser, const QString& aPwd, unsigned int aTasks, bool aUse4Api)
: DirtyListVisit(aDoc, aFuture, true), Tasks(aTasks), Done(0), Web(aWeb), User(aUser), Pwd(aPwd), Use4Api(aUse4Api), theDownloader(0)
{
	theDownloader = new Downloader(Web, User, Pwd);
}

DirtyListExecutor::~DirtyListExecutor()
{
	delete theDownloader;
}


bool DirtyListExecutor::sendRequest(const QString& Method, const QString& URL, const QString& Data, QString& Rcv)
{
	if (!theDownloader->request(Method,URL,Data))
		return false;

	QByteArray Content = theDownloader->content();
	int x = theDownloader->resultCode();
	
	if (x==200)
	{
		Rcv = QString::fromUtf8(Content.data());
		return true;
	}
	else
		QMessageBox::warning(Progress,tr("Error uploading request"),tr("There was an error uploading this request (%1)").arg(x));
	return false;

}

bool DirtyListExecutor::executeChanges(QWidget* aParent)
{
	Progress = new QProgressDialog(aParent);
	Progress->setMinimumDuration(0);
	Progress->setMaximum(Tasks);
	Progress->show();
	document()->history().buildDirtyList(*this);
	delete Progress;
	return true;
}

bool DirtyListExecutor::addWay(Way* W)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("ADD link %1").arg(W->id()) + userName(W));
	if (W->controlFrom() || W->controlTo())
		return false;
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = W->id();
	W->setId("0");
	DataIn = wrapOSM(exportOSM(*W));
	W->setId(OldId);
//	QString URL("/api/0.3/segment/0");
	QString URL=theDownloader->getURLToCreate("segment");
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		W->setId("segment_"+QString::number(DataOut.toInt()));
		W->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyListExecutor::addRoad(Road *R)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("ADD road %1").arg(R->id()) + userName(R));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = R->id();
	R->setId("0");
	DataIn = wrapOSM(exportOSM(*R));
	R->setId(OldId);
//	QString URL("/api/0.3/way/0");
	QString URL=theDownloader->getURLToCreate("way");
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		R->setId("way_"+QString::number(DataOut.toInt()));
		R->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}


bool DirtyListExecutor::addPoint(TrackPoint* Pt)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("ADD trackpoint %1").arg(Pt->id()) + userName(Pt));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);

	QString DataIn, DataOut, OldId;
	OldId = Pt->id();
	Pt->setId("0");
	DataIn = wrapOSM(exportOSM(*Pt));
	Pt->setId(OldId);
//	QString URL("/api/0.3/node/0");
	QString URL=theDownloader->getURLToCreate("node");
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		// chop off extra spaces, newlines etc
		Pt->setId("node_"+QString::number(DataOut.toInt()));
		Pt->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}


bool DirtyListExecutor::updateWay(Way* W)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("UPDATE link %1").arg(W->id()) + userName(W));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/segment/%1");
//	URL = URL.arg(stripToOSMId(W->id()));
	QString URL = theDownloader->getURLToUpdate("segment",stripToOSMId(W->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*W));
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		W->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return true;
}

bool DirtyListExecutor::updateRoad(Road* R)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("UPDATE road %1").arg(R->id()) + userName(R));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/way/%1");
//	URL = URL.arg(stripToOSMId(R->id()));
	QString URL = theDownloader->getURLToUpdate("way",stripToOSMId(R->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*R));
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		R->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return true;
}

bool DirtyListExecutor::updatePoint(TrackPoint* Pt)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("UPDATE trackpoint %1").arg(Pt->id()) + userName(Pt));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/node/%1");
//	URL = URL.arg(stripToOSMId(Pt->id()));
	QString URL = theDownloader->getURLToUpdate("node",stripToOSMId(Pt->id()));
	QString DataIn, DataOut;
	DataIn = wrapOSM(exportOSM(*Pt));
	if (sendRequest("PUT",URL,DataIn,DataOut))
	{
		Pt->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyListExecutor::erasePoint(TrackPoint *Pt)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("REMOVE trackpoint %1").arg(Pt->id()) + userName(Pt));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/node/%1");
//	URL = URL.arg(stripToOSMId(Pt->id()));
	QString URL = theDownloader->getURLToDelete("node",stripToOSMId(Pt->id()));
	QString DataIn, DataOut;
	if (sendRequest("DELETE",URL,DataIn,DataOut))
	{
		Pt->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyListExecutor::eraseRoad(Road *R)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("REMOVE road %1").arg(R->id()) + userName(R));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/way/%1");
//	URL = URL.arg(stripToOSMId(R->id()));
	QString URL = theDownloader->getURLToDelete("way",stripToOSMId(R->id()));
	QString DataIn, DataOut;
	if (sendRequest("DELETE",URL,DataIn,DataOut))
	{
		R->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}

bool DirtyListExecutor::eraseWay(Way *W)
{
	Progress->setValue(++Done);
	Progress->setLabelText(QString("REMOVE link %1").arg(W->id()) + userName(W));
	QEventLoop L; L.processEvents(QEventLoop::ExcludeUserInputEvents);
//	QString URL("/api/0.3/segment/%1");
//	URL = URL.arg(stripToOSMId(W->id()));
	QString URL = theDownloader->getURLToDelete("segment",stripToOSMId(W->id()));
	QString DataIn, DataOut;
	if (sendRequest("DELETE",URL,DataIn,DataOut))
	{
		W->setLastUpdated(MapFeature::OSMServer);
		return true;
	}
	return false;
}
