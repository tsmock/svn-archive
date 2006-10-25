#include "Map/ImportGPX.h"

#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Map/MapDocument.h"
#include "Map/TrackPoint.h"
#include "Map/TrackSegment.h"

#include <QtCore/QFile>
#include <QtGui/QMessageBox>
#include <QtXml/QDomDocument>


static TrackPoint* importTrkPt(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, CommandList* theList)
{
	double Lat = Root.attribute("lat").toDouble();
	double Lon = Root.attribute("lon").toDouble();
	TrackPoint* Pt = new TrackPoint(Coord(angToRad(Lat),angToRad(Lon)));
	theList->add(new AddFeatureCommand(theLayer,Pt, true));
	return Pt;
}


static void importTrkSeg(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, CommandList* theList)
{
	TrackSegment* S = new TrackSegment;
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull() && t.tagName() == "trkpt")
		{
			TrackPoint* Pt = importTrkPt(t,theDocument, theLayer, theList);
			S->add(Pt);
		}
	}
	if (S->size())
		theList->add(new AddFeatureCommand(theLayer,S, true));
	else
		delete S;
}

static void importTrk(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, CommandList* theList)
{
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull() && t.tagName() == "trkseg")
			importTrkSeg(t,theDocument, theLayer, theList);
	}
}

static void importGPX(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, CommandList* theList)
{
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull() && t.tagName() == "trk")
			importTrk(t,theDocument, theLayer, theList);
	}
}

bool importGPX(QWidget* aParent, const QString& aFilename, MapDocument* theDocument, MapLayer* theLayer)
{
	// TODO remove debug messageboxes
	QFile File(aFilename);
	if (!File.open(QIODevice::ReadOnly))
		 return false;
	QDomDocument DomDoc;
	QString ErrorStr;
	int ErrorLine;
	int ErrorColumn;
	if (!DomDoc.setContent(&File, true, &ErrorStr, &ErrorLine,&ErrorColumn))
	{
		File.close();
		QMessageBox::warning(aParent,"Parse error",
			QString("Parse error at line %1, column %2:\n%3")
                                  .arg(ErrorLine)
                                  .arg(ErrorColumn)
                                  .arg(ErrorStr));
		return false;
	}
	QDomElement root = DomDoc.documentElement();
	if (root.tagName() != "gpx")
	{
		QMessageBox::information(aParent, "Parse error","Root is not a gpx node");
		return false;
	}
	CommandList* theList = new CommandList;
	theDocument->add(theLayer);
	importGPX(root, theDocument, theLayer, theList);
	if (theList->empty())
		delete theList;
	else
		theDocument->history().add(theList);
	return true;
}


