#ifndef PACKET_FACTORY_H
#define PACKET_FACTORY_H

#include "OutputStream.hpp"
#include "OutputPacket.hpp"
#include "UnknownPacket.hpp"
#include "PacketHandler.hpp"
#include "PacketExtractor.hpp"
#include <map>

extern "C" {
#include <pthread.h>
#include <semaphore.h>
}

namespace SiRF {

  class PacketFactory {

    typedef std::map<uint8, PacketExtractorBase *> PEMap;

  public:

    /* initialise self on basis of existing stream
     */
    PacketFactory(OutputStream &_in);

    /* get a packet
     */
    OutputPacket *getPacket();

    /* register a packet handler.
     * in the event loop that packet handler will recieve packets of the
     * given type
     */
    template <class T>
    void registerHandler(PacketHandler<T> *ph) {
      // afaik, this can never fail, since multimap allows non-unique
      // keys and values!
      if (extractors.count(T::type) == 0) {
	extractors[T::type] = new PacketExtractor<T>;
      }
      static_cast<PacketExtractor<T>*>(extractors[T::type])->add(ph);
    }

    /*
     */
    void registerDefaultHandler(PacketHandler<UnknownPacket> *ph) {
      defaultHandlers.add(ph);
    }

    /* go into a loop for packets
     */
    void eventLoop();

    /* exit the loop
     */
    void exitLoop();

    /*
     */
    void throwAwayPacketsUntilNice();

  private:

    /* allocate and return the right packet type
     */
    OutputPacket *getNewPacketForType(uint8 type);

    /* the stream used as input
     */
    OutputStream &in;

    /* map the packet types to packet handlers
     */
    PEMap extractors;

    /* default handlers
     */
    PacketExtractor<UnknownPacket> defaultHandlers;

    /* underlying semaphore and thread
     */
    ::sem_t lock;
    ::pthread_t thread;

    /* locking functions
     */
    bool goodToContinue();
    void threadedEventLoop();

    /* stupid hacked-up function to do pthreaded-ness
     */
    static void *stupidHackedUpPthreadFunction(void *);

  };

}

#endif /* PACKET_FACTORY_H */
