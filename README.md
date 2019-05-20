# Poulet
Implementing a dependently typed language. To execute code written in Poulet, simply execute the jar with the file to run as the first argument followed by a list of libraries to link for imports:
```
java -jar Poulet.jar <file_name> <library_one> <library_two> ...
```
By default libraries are not searched recursively when searching for imported files. To change this you can add the `-r` flag and any library directories following this flag will be searched recursively. An example command looks like
```
java -jar Poulet.jar <file_name> <library_one> <library_two> ... -r <recur_library_one> <recur_library_two> ...
```
