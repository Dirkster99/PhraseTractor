@ECHO OFF
ECHO.
ECHO Creating PhraseTractor.jar file
ECHO.
cd bin

jar cfm ..\PhraseTractor.jar ..\src\MANIFEST.MF *

cd ..
