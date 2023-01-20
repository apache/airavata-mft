import typer
import requests
import os
import zipfile
from subprocess import call
from subprocess import Popen
from pathlib import Path

def download_and_unarchive(url, download_path, extract_dir = os.path.join(os.path.expanduser('~'), ".mft/")):

  response = requests.get(url, stream=True)
  file_size = int(response.headers['Content-Length'])
  with typer.progressbar(length=file_size) as progress:
    with open(download_path, "wb") as handle:
      for data in response.iter_content(chunk_size=8192 * 2):
        progress.update(len(data))
        handle.write(data)

  print("Un archiving ....")
  with zipfile.ZipFile(download_path,"r") as zip_ref:
    zip_ref.extractall(extract_dir)

  os.remove(download_path)

def restart_service(bin_path, daemon_script_name):
  current_dir =  os.getcwd()
  try:
    os.chdir(bin_path)
    os.chmod(daemon_script_name, 0o744)
    rc = call(["./" + daemon_script_name, "stop"])
    rc = call(["./" + daemon_script_name, "start"])
  finally:
    os.chdir(current_dir)

def stop_service(bin_path, daemon_script_name):
  current_dir =  os.getcwd()
  try:
    os.chdir(bin_path)
    os.chmod(daemon_script_name, 0o744)
    rc = call(["./" + daemon_script_name, "stop"])
  finally:
    os.chdir(current_dir)

def start_mft():
  print("Setting up MFT Services")

  path = os.path.join(os.path.expanduser('~'), ".mft/consul")
  if not os.path.exists(path):
    consul_macos_url = "https://releases.hashicorp.com/consul/1.7.1/consul_1.7.1_darwin_amd64.zip"
    consul_linux_url = "https://releases.hashicorp.com/consul/1.7.1/consul_1.7.1_linux_amd64.zip"
    print("Downloading Consul...")
    zip_path = os.path.join(os.path.expanduser('~'), ".mft/consul.zip")
    download_and_unarchive(consul_macos_url, zip_path, os.path.join(os.path.expanduser('~'), ".mft/"))

  current_dir =  os.getcwd()
  try:
    os.chdir(os.path.join(os.path.expanduser('~'), ".mft"))
    os.chmod("consul", 0o744)

    if os.path.exists("consul.pid"):
      pid = Path('consul.pid').read_text()
      call(["kill", "-9", pid])

    consul_process = Popen(['nohup', './consul', "agent", "-dev"],
                     stdout=open('consul.log', 'w'),
                     stderr=open('consul.err.log', 'a'),
                     preexec_fn=os.setpgrp)

    print("Consul process id: " + str(consul_process.pid))
    with open("consul.pid", "w") as consul_pid:
      consul_pid.write(str(consul_process.pid))
  finally:
    os.chdir(current_dir)

  path = os.path.join(os.path.expanduser('~'), ".mft/Standalone-Service-0.01")
  if not os.path.exists(path):
    url = "https://github.com/apache/airavata-mft/releases/download/v0.0.1/Standalone-Service-0.01-bin.zip"
    print("Downloading MFT Server...")
    zip_path = os.path.join(os.path.expanduser('~'), ".mft/Standalone-Service-0.01-bin.zip")
    download_and_unarchive(url, zip_path)

  restart_service(path + "/bin", "standalone-service-daemon.sh")

  print("MFT Started")


def stop_mft():
  print("Stopping up MFT Services")

  path = os.path.join(os.path.expanduser('~'), ".mft/consul")
  if os.path.exists(path):
    current_dir =  os.getcwd()
    try:
      os.chdir(os.path.join(os.path.expanduser('~'), ".mft"))
      os.chmod("consul", 0o744)

      if os.path.exists("consul.pid"):
        pid = Path('consul.pid').read_text()
        call(["kill", "-9", pid])
    finally:
      os.chdir(current_dir)

  path = os.path.join(os.path.expanduser('~'), ".mft/Standalone-Service-0.01")
  if os.path.exists(path):
    stop_service(path + "/bin", "standalone-service-daemon.sh")

  print("MFT Stopped....")




