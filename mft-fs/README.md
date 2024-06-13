<h1>MFT-FS</h1>

<hr>

MFT-FS is a FUSE-based extension to Airavata MFT. It provides the abstraction that unifies the different file I/O protocols into one filesystem.

<h3>Instructions</h3>

<code>go.mod</code> (with <code>go.sum</code> file) file in the <code>mft-fs</code> directory

To configure the mod and sum files:

<ol>
    <li>Make the <code>go.mod</code> file: <code>go mod init mft-fs</code></li>
    <li>Install Dependencies
        <ul>
            <li><code>go get github.com/jacobsa/fuse</code></li>
        </ul>
    </li>
</ol>


To build the project, run the build command from the <code>main</code> subfolder:

<code>go build</code>

Before running, ensure you have the <code>mount</code> and <code>test</code> directories in the <code>main</code> directory.

```
mkdir mount
mkdir test
```

Finally, run the executable file:

<code>./main</code>

<strong>Important Final Step:</strong> Enjoy and report any bugs!