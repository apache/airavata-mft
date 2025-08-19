package main

import (
	"context"
	"flag"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseutil"
	"log"
	"mft-fs/archive/abstractfs"
	"mft-fs/archive/osfsmanager"
	"os"
)

func main() {

	// reading commandline args
	mountDirectory := flag.String("mountDirectory", "", "mount directory")
	rootDirectory := flag.String("rootDirectory", "", "root directory")
	flag.Parse()

	manager := osfsmanager.NewOSFSManager(*rootDirectory)
	fs, _ := abstractfs.NewAbstractFS(manager)
	server := fuseutil.NewFileSystemServer(&fs)

	// mount the filesystem
	cfg := &fuse.MountConfig{
		ReadOnly:    false,
		DebugLogger: log.New(os.Stderr, "fuse: ", 0),
		ErrorLogger: log.New(os.Stderr, "fuse: ", 0),
	}
	mfs, err := fuse.Mount(*mountDirectory, server, cfg)
	if err != nil {
		log.Fatalf("Mount: %v", err)
	}

	// wait for it to be unmounted
	if err = mfs.Join(context.Background()); err != nil {
		log.Fatalf("Join: %v", err)
	}
}
