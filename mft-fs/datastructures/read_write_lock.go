package datastructures

import (
	"sync"
)

type CREWResource struct {
	reads      int
	readsMutex *sync.Mutex
	doneReads  *sync.Cond

	allowRead      bool
	allowReadMutex *sync.Mutex
	doneWrite      *sync.Cond
}

func NewCREWResource() *CREWResource {
	mutex1 := &sync.Mutex{}
	mutex2 := &sync.Mutex{}

	return &CREWResource{
		reads:      0,
		readsMutex: mutex1,
		doneReads:  &sync.Cond{L: mutex1},

		allowRead:      true,
		allowReadMutex: mutex2,
		doneWrite:      &sync.Cond{L: mutex2},
	}
}

func (resource *CREWResource) RequestRead() {
	resource.allowReadMutex.Lock()
	for !resource.allowRead {
		resource.doneWrite.Wait()
	}
	resource.readsMutex.Lock()
	resource.reads++
	resource.readsMutex.Unlock()
	resource.allowReadMutex.Unlock()
}

func (resource *CREWResource) AckRead() {
	resource.readsMutex.Lock()
	resource.reads--
	if resource.reads == 0 {
		resource.doneReads.Signal()
	}
	resource.readsMutex.Unlock()
}

func (resource *CREWResource) RequestWrite() {
	// wait for any writes to finish and set allow reads to false
	resource.allowReadMutex.Lock()
	for !resource.allowRead {
		resource.doneWrite.Wait()
	}
	resource.allowRead = false
	resource.allowReadMutex.Unlock()

	// wait for all reads to finish
	resource.readsMutex.Lock()
	for resource.reads > 0 {
		resource.doneReads.Wait()
	}
	resource.readsMutex.Unlock()
}

func (resource *CREWResource) AckWrite() {
	resource.allowReadMutex.Lock()
	resource.allowRead = true
	resource.doneWrite.Broadcast()
	resource.allowReadMutex.Unlock()
}
