package main

import (
	"bufio"
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"math/rand"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseutil"

	"mft-fs/abstractfs"
	"mft-fs/osfs"
	"mft-fs/sftpfs"
)

// --------------- GLOBAL VARIABLES ---------------------

var mftfsDir string = "/Users/rohanphadnis/GeorgiaTech/ArtisanResearch/airavata-mft/mft-fs/mftfs_data"
var infoData *InfoFile

const IdLength uint8 = 16

// ------------- DATA STRUCTURES -------------------------

// InfoFile represents the structure of the info.json file.
type InfoFile struct {
	PathToID map[string]string          `json:"path_to_id"`
	IDToData map[string]FilesystemEntry `json:"id_to_data"`
}

func (info *InfoFile) generateId() string {
	output := make([]byte, IdLength)
	for i := uint8(0); i < IdLength; i++ {
		output[i] = uint8(rand.Float64()*26 + 65)
	}
	_, ok := info.IDToData[string(output)]
	if ok {
		return info.generateId()
	}
	return string(output)
}

func (info *InfoFile) AddNewFs(mountDir string, fsType string, fsConfig *Config) {
	identity := info.generateId()
	info.IDToData[identity] = FilesystemEntry{
		ID:     identity,
		Path:   mountDir,
		Type:   fsType,
		Config: fsConfig,
	}
	info.PathToID[mountDir] = identity

	err := writeInfo()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error writing info file: %v\n", err)
	}
}

type Config struct {
	SftpConfig *sftpfs.SftpClientConfig
	OsfsConfig *osfs.Config
	// add other configuration variables
}

// FilesystemEntry holds the metadata for a single mounted filesystem.
type FilesystemEntry struct {
	ID     string  `json:"id"`
	Path   string  `json:"path"`
	Type   string  `json:"type"`
	Config *Config `json:"config"`
}

// Command-line flags.
var (
	helpFlag    bool
	initFlag    string
	destroyFlag string
	infoFlag    bool
)

func init() {
	flag.BoolVar(&helpFlag, "help", false, "Display help information for the program.")
	flag.StringVar(&initFlag, "init", "", "Initialize and mount a filesystem at the specified directory.")
	flag.StringVar(&destroyFlag, "destroy", "", "Unmount and destroy the filesystem at the specified directory.")
	flag.BoolVar(&infoFlag, "info", false, "Display information about the mounted filesystem.")
}

// --------------- MAIN METHOD ------------------

func main() {
	flag.Parse()

	// Perform a one-time setup to ensure the metadata directory exists.
	if err := startup(); err != nil {
		fmt.Fprintf(os.Stderr, "Startup failed: %v\n", err)
		os.Exit(1)
	}

	// Read the info.json file into memory.
	if err := readInfo(); err != nil {
		fmt.Fprintf(os.Stderr, "Failed to read info.json: %v\n", err)
		os.Exit(1)
	}

	// Handle the command-line flags.
	switch {
	case helpFlag:
		handleHelp()
	case initFlag != "":
		handleInit(initFlag)
		runFilesystem(initFlag)
	case destroyFlag != "":
		handleDestroy(destroyFlag)
	case infoFlag:
		handleInfo()
	default:
		handleHelp()
	}
}

func runFilesystem(mountDir string) {
	fmt.Println("running filesystem!")

	absMountDir, err := filepath.Abs(mountDir)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Invalid mount directory path: %v\n", err)
		os.Exit(1)
	}

	id, ok := infoData.PathToID[absMountDir]
	if !ok {
		fmt.Fprintf(os.Stderr, "Could not find mount directory %s\n", absMountDir)
		os.Exit(1)
	}

	fsInfo, ok := infoData.IDToData[id]
	if !ok {
		fmt.Fprintf(os.Stderr, "Could not find file system info for %s\n", id)
		os.Exit(1)
	}

	if fsInfo.Config == nil {
		fmt.Fprintln(os.Stderr, "Null value of configuration")
		os.Exit(1)
	}

	var fs *abstractfs.AbstractFS

	switch fsInfo.Type {
	case "osfs":
		fs = osfs.NewOSFS(absMountDir, fsInfo.Config.OsfsConfig)
	case "sftpfs":
		fs = sftpfs.NewSFTPFS(absMountDir, fsInfo.Config.SftpConfig)
	default:
		fmt.Fprintf(os.Stderr, "Filesystem type %s not implemented\n", fsInfo.Type)
		os.Exit(1)
	}

	if fs == nil {
		fmt.Fprintf(os.Stderr, "Could not find mount directory 678 %s\n", absMountDir)
		os.Exit(1)
	}

	err = fs.Manager.Start()
	if err != nil {
		fmt.Fprintf(os.Stderr, "Startup failed: %v\n", err)
		os.Exit(1)
	}
	server := fuseutil.NewFileSystemServer(fs)

	// mount the filesystem
	cfg := &fuse.MountConfig{
		ReadOnly:    false,
		DebugLogger: log.New(os.Stderr, "fuse: ", 0),
		ErrorLogger: log.New(os.Stderr, "fuse: ", 0),
	}
	mfs, err := fuse.Mount(mountDir, server, cfg)
	if err != nil {
		log.Fatalf("Mount: %v", err)
	}

	// wait for it to be unmounted
	if err = mfs.Join(context.Background()); err != nil {
		log.Fatalf("Join: %v", err)
	}
}

// startup ensures the .mftfs directory structure exists.
func startup() error {
	/*
		todo: uncomment this for eventual release
		homeDir, err := os.UserHomeDir()
		if err != nil {
			return fmt.Errorf("could not find user home directory: %w", err)
		}

		mftfsDir = filepath.Join(homeDir, ".mftfs")*/

	infoPath := filepath.Join(mftfsDir, "info.json")
	cacheDir := filepath.Join(mftfsDir, "cache")

	// Create .mftfs directory if it doesn't exist.
	if err := os.MkdirAll(mftfsDir, 0755); err != nil {
		return fmt.Errorf("failed to create .mftfs directory: %w", err)
	}

	// Create cache directory if it doesn't exist.
	if err := os.MkdirAll(cacheDir, 0755); err != nil {
		return fmt.Errorf("failed to create cache directory: %w", err)
	}

	// Create empty info.json file if it doesn't exist.
	if _, err := os.Stat(infoPath); os.IsNotExist(err) {
		initialInfo := InfoFile{
			PathToID: make(map[string]string),
			IDToData: make(map[string]FilesystemEntry),
		}
		data, err := json.MarshalIndent(initialInfo, "", "  ")
		if err != nil {
			return fmt.Errorf("failed to marshal initial info.json: %w", err)
		}
		if err := ioutil.WriteFile(infoPath, data, 0644); err != nil {
			return fmt.Errorf("failed to write initial info.json: %w", err)
		}
	}

	return nil
}

// ----------------- INFO FILE UTILITIES -------------------

// readInfo loads the metadata from the info.json file.
func readInfo() error {
	infoPath := filepath.Join(mftfsDir, "info.json")
	data, err := ioutil.ReadFile(infoPath)
	if err != nil {
		return fmt.Errorf("failed to read info.json: %w", err)
	}

	infoData = &InfoFile{}
	if err := json.Unmarshal(data, infoData); err != nil {
		return fmt.Errorf("failed to unmarshal info.json: %w", err)
	}

	// Initialize maps if they were nil after unmarshalling an empty JSON object.
	if infoData.PathToID == nil {
		infoData.PathToID = make(map[string]string)
	}
	if infoData.IDToData == nil {
		infoData.IDToData = make(map[string]FilesystemEntry)
	}

	return nil
}

// writeInfo saves the current in-memory infoData to info.json.
func writeInfo() error {
	data, err := json.MarshalIndent(infoData, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal infoData: %w", err)
	}
	infoPath := filepath.Join(mftfsDir, "info.json")
	if err := ioutil.WriteFile(infoPath, data, 0644); err != nil {
		return fmt.Errorf("failed to write info.json: %w", err)
	}
	return nil
}

// ------------------- CLI HANDLERS -----------------

// handleHelp displays the usage information for the CLI tool.
func handleHelp() {
	fmt.Println("Usage: mftfs [flags]")
	fmt.Println("A command-line tool to manage your FUSE filesystems.")
	fmt.Println("\nFlags:")
	flag.PrintDefaults()
}

// handleInit guides the user through the filesystem initialization process.
func handleInit(mountDir string) {
	reader := bufio.NewReader(os.Stdin)
	absMountDir, err := filepath.Abs(mountDir)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Invalid mount directory path: %v\n", err)
		os.Exit(1)
	}

	// Check if the mount path exists in the metadata.
	fsID, ok := infoData.PathToID[absMountDir]
	if ok {
		fsEntry, entryExists := infoData.IDToData[fsID]
		if !entryExists {
			fmt.Println("Error: Metadata inconsistency detected.")
			os.Exit(1)
		}

		fmt.Printf("There was previously a filesystem of type %s on this path. Do you want to relaunch it? [y/n]: ", fsEntry.Type)
		response := readInput(reader, "")

		if strings.ToLower(response) == "y" {
			fmt.Printf("Relaunching existing %s filesystem at %s.\n", fsEntry.Type, absMountDir)
			// TODO: Add logic to use fsEntry.Config to mount the filesystem.
			return
		}

		if strings.ToLower(response) == "n" {
			// User wants a new filesystem, so remove the old metadata.
			delete(infoData.PathToID, absMountDir)
			delete(infoData.IDToData, fsID)
			if err := writeInfo(); err != nil {
				fmt.Fprintf(os.Stderr, "Failed to update info.json: %v\n", err)
				os.Exit(1)
			}
			fmt.Println("Removed old filesystem metadata. Starting a new initialization.")
		} else {
			fmt.Println("Invalid response. Exiting.")
			os.Exit(1)
		}
	}

	// Normal initialization flow if no previous record exists or user wants a new one.
	fmt.Printf("Initializing a new filesystem at: %s\n", absMountDir)
	fsType := strings.ToLower(readInput(reader, "Type of filesystem (s3fs, osfs, grpcfs, sftpfs): "))

	config := &Config{}

	switch fsType {
	case "sftpfs":
		handleSFTPFSInit(reader, config)
	case "osfs":
		handleOsfsInit(reader, config)
	case "s3fs", "grpcfs":
		fmt.Printf("Support for %s is not yet implemented.\n", fsType)
		os.Exit(1)
	default:
		fmt.Printf("Unknown filesystem type: %s\n", fsType)
		os.Exit(1)
	}

	infoData.AddNewFs(absMountDir, fsType, config)

	fmt.Printf("Filesystem of type '%s' is ready to be mounted at '%s'.\n", fsType, absMountDir)
	fmt.Println("Add your mounting logic here.")
}

// handleDestroy is a placeholder for the destroy logic.
func handleDestroy(mountDir string) {
	fmt.Printf("Attempting to unmount and destroy filesystem at: %s\n", mountDir)
	fmt.Println("Add your unmounting logic here.")
}

// handleInfo displays the contents of the info.json file in a pretty-printed format.
func handleInfo() {
	fmt.Println("Displaying metadata from info.json:")
	data, err := json.MarshalIndent(infoData, "", "  ")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error marshaling info data for display: %v\n", err)
		return
	}
	fmt.Println(string(data))
}

// -------------- FILESYSTEM SPECIFIC HANDLERS ------------------------

// handleSFTPFSInit prompts for and reads SFTP connection details and returns the config map.
func handleSFTPFSInit(reader *bufio.Reader, config *Config) {
	fmt.Println("\n--- SFTP-FS Configuration ---")
	host := readInput(reader, "Host: ")
	p, err := strconv.ParseInt(readInput(reader, "Port: "), 10, 16)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Invalid port: %v\n", err)
		os.Exit(1)
	}
	port := uint16(p)
	username := readInput(reader, "Username: ")
	password := readInput(reader, "Password: ")

	config.SftpConfig = &sftpfs.SftpClientConfig{
		Host:     host,
		Port:     port,
		Username: username,
		Password: password,
		// PrivateKeyPath: ".",
		RemoteRoot: ".",
	}
}

// handleS3FSInit prompts for and reads S3 connection details and returns the config map.
func handleOsfsInit(reader *bufio.Reader, config *Config) {
	fmt.Println("\n--- OS-FS Configuration ---")
	rootDir := readInput(reader, "Root Absolute Path: ")

	config.OsfsConfig = &osfs.Config{RootDir: rootDir}
}

// handleS3FSInit prompts for and reads S3 connection details and returns the config map.
func handleS3FSInit(reader *bufio.Reader) {
	fmt.Println("\n--- S3-FS Configuration ---")
	// todo: implement
	/*
		bucketName := readInput(reader, "Bucket Name: ")
		region := readInput(reader, "Region: ")
		awsKey := readInput(reader, "AWS Key: ")
		awsSecretKey := readInput(reader, "AWS Secret Key: ")
	*/
}

// ----------------- INFO FILE UTILS -----------------

// readInput is a helper function to read a line of input from the user.
func readInput(reader *bufio.Reader, prompt string) string {
	fmt.Print(prompt)
	input, _ := reader.ReadString('\n')
	return strings.TrimSpace(input)
}
