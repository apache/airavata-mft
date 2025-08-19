# Use the latest Ubuntu image
FROM ubuntu:latest

# Install necessary packages
RUN apt-get update && apt-get install -y \
    fuse \
    kmod \
    wget \
    curl \
    git \
    build-essential

# Download and install Go v1.22.3
RUN wget https://go.dev/dl/go1.22.3.linux-amd64.tar.gz && \
    tar -C /usr/local -xzf go1.22.3.linux-amd64.tar.gz && \
    rm go1.22.3.linux-amd64.tar.gz

# Set up Go environment
ENV PATH="/usr/local/go/bin:${PATH}"
ENV GOPATH="/go"
ENV PATH="${GOPATH}/bin:${PATH}"

