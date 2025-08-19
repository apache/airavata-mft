package main

import (
	"context"
	"flag"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseutil"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"log"
	"mft-fs/archive/abstractfs"
	"mft-fs/archive/remotefs/client"
	"os"
)

func main() {

	// reading commandline args
	mountDirectory := flag.String("mountDirectory", "", "mount directory")
	flag.Parse()

	// setting up gRPC communicators
	var opts []grpc.DialOption
	opts = append(opts, grpc.WithTransportCredentials(insecure.NewCredentials()))
	conn, e := grpc.NewClient(":8000", opts...)
	if e != nil {
		log.Fatalf("Error: %v\n", e)
	}
	defer conn.Close()

	// setting up FUSE
	manager, e := client.NewClientManager(conn)
	if e != nil {
		log.Fatalf("Error: %v\n", e)
	}
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
