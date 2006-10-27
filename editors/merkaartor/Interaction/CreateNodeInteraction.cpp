#include "CreateNodeInteraction.h"

#include "MainWindow.h"
#include "PropertiesDock.h"
#include "Command/DocumentCommands.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"
#include "Utils/LineF.h"

#include <vector>

CreateNodeInteraction::CreateNodeInteraction(MapView* aView)
: WaySnapInteraction(aView)
{
}

CreateNodeInteraction::~CreateNodeInteraction(void)
{
}

void CreateNodeInteraction::snapMouseReleaseEvent(QMouseEvent * ev, Way* aWay)
{
	Coord P(projection().inverse(ev->pos()));
	if (aWay && !aWay->controlFrom())
	{
		main()->properties()->setSelection(0);
		CommandList* theList = new CommandList;
		LineF L(aWay->from()->position(),aWay->to()->position());
		TrackPoint* N = new TrackPoint(L.project(P));
		theList->add(new AddFeatureCommand(main()->activeLayer(),N,true));
		Way* W1 = new Way(aWay->from(),N);
		theList->add(new AddFeatureCommand(main()->activeLayer(),W1,true));
		Way* W2 = new Way(N,aWay->to());
		theList->add(new AddFeatureCommand(main()->activeLayer(),W2,true));
		std::vector<MapFeature*> Alternatives;
		Alternatives.push_back(W1);
		Alternatives.push_back(W2);
		for (FeatureIterator it(document()); !it.isEnd(); ++it)
			it.get()->cascadedRemoveIfUsing(document(), aWay, theList, Alternatives);
		theList->add(new RemoveFeatureCommand(document(),aWay));
		document()->history().add(theList);
		view()->update();
	}
	else
	{
		TrackPoint* N = new TrackPoint(P);
		document()->history().add(new AddFeatureCommand(main()->activeLayer(),N,true));
		view()->update();
	}
}


QCursor CreateNodeInteraction::cursor() const
{
	return QCursor(Qt::CrossCursor);
}




