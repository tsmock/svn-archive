#include "Interaction/CreateRoadInteraction.h"

#include "MainWindow.h"
#include "MapView.h"
#include "PropertiesDock.h"
#include "Command/DocumentCommands.h"
#include "Command/RoadCommands.h"
#include "Map/MapDocument.h"
#include "Map/Road.h"
#include "Map/Way.h"

#include <QtGui/QMouseEvent>

CreateRoadInteraction::CreateRoadInteraction(MapView* aView)
: WaySnapInteraction(aView), Current(0)
{
	main()->properties()->setSelection(0);
}

CreateRoadInteraction::CreateRoadInteraction(MapView* aView, Road* R)
: WaySnapInteraction(aView), Current(R)
{
	main()->properties()->setSelection(0);
}

CreateRoadInteraction::~CreateRoadInteraction(void)
{
}

void CreateRoadInteraction::paintEvent(QPaintEvent* event, QPainter& thePainter)
{
	WaySnapInteraction::paintEvent(event,thePainter);
	if (Current)
		Current->drawFocus(thePainter,projection());
}

void CreateRoadInteraction::snapMouseReleaseEvent(QMouseEvent *anEvent, Way * W)
{
	// TODO warn to click on ways?
	if (!W) return;
	if (!Current)
	{
		Current = new Road;
		Current->add(W);
		W->addAsPartOf(Current);
		document()->history().add(new AddFeatureCommand( main()->activeLayer() ,Current, true));
		main()->properties()->setSelection(Current);
	}
	else
	{
		if (Current->find(W) < Current->size())
			document()->history().add(new RoadRemoveWayCommand(Current, W));
		else
			document()->history().add(new RoadAddWayCommand(Current, W));
	}
	Current->setLastUpdated(MapFeature::User);
	view()->invalidate();
}




