package main

import (
	"context"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseutil"
	"log"
	"mft-fs/basicfs"
	"os"
)

func main() {

	// create an appropriate file system
	// printer("started")
	fs, _ := basicfs.NewBasicFS("./test")
	server := fuseutil.NewFileSystemServer(&fs)

	// mount the filesystem
	cfg := &fuse.MountConfig{
		ReadOnly:    false,
		DebugLogger: log.New(os.Stderr, "fuse: ", 0),
		ErrorLogger: log.New(os.Stderr, "fuse: ", 0),
	}
	mfs, err := fuse.Mount("./mount", server, cfg)
	if err != nil {
		log.Fatalf("Mount: %v", err)
	}

	// wait for it to be unmounted
	if err = mfs.Join(context.Background()); err != nil {
		log.Fatalf("Join: %v", err)
	}
}
