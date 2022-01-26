
# Pre-Requisites

The **PhraseTractor** tool is a Java tool and, as such, its a good idea to have:
- a [Java Runtime](https://www.oracle.com/java/technologies/downloads/) (JRE) installed to execute the JAR file and have
- a [Java Development Kit](https://www.oracle.com/java/technologies/downloads/) (JDK) to compile, debug, and compile the tool

I am also using [Visual Studio Code](https://code.visualstudio.com/) as an editor to debug, run or edit Java.

# Building on Windows

While Java compiles and runs on way more operating systems than Windows I am running on Windows and am, therefore, able to describe my setup on this system only (at this time). But I am sure other operating systems like Unix or Mac have similar requirements. Let me know if you have trouble on another OS or contribute a build description for your OS if its other than Windows :-)

On Windows, you can work with the *.bat files at the root of the PhraseTractor's source folder.

- Open the ```AddJavaPath.bat``` file in an editor and adjust the **JDK** path to point to a valid directory on your machine
- Open the ```CMD``` command prompt and ```cd``` into the folder of the above batch file
- Execute ```compile.bat``` and check the ```bin``` folder as it should no contain the compiled ```.class``` files
- Execute ```build_jar.bat``` and check the **root** folder of the project as it should now contain the ```PhraseTractor.jar``` files

If this went as described you are done setting up your system for the development of this tool.
Follow an (additional guide)[./docs/PhraseTractor.pdf] to learn more about its usage with text.
