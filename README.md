# Cryptography-Term-Project
Implementation of a Basic Blockchain in Java
You can take a look at the source code in the SOURCE folder

## How to run the program
1. Make sure that the windows pc has java version 13 or higher.
2. Open command prompt in admin mode and type the following command: 
java -jar "file path". It will run BLOCKCHAIN.jar.
Example: java -jar "C:\Users\anild\Downloads\export methods\runnable jar file\BLOCKCHAIN.jar" where executable is present in C:\Users\anild\Downloads\export methods\runnable jar file\ directory
3. If the program encounters any error during its operation run the executable named SAFEMODE.jar


## Steps to compile the blockchain from source. code using Eclipse IDE


1. Goto File>Import>General>Archive File and click on next.
2. In the dialog box, select the .zip file named BLOCKCHAIN.zip
3. Import bouncy castle library. It can be found in the folder with the name bcprov-jdk15on-165.jar
*  Open Windows >preferences in the Eclipse menu, and navigate to the Java>Build path > User Libraries tab. Click new and enter a new User Library name: like “bouncycastle_lib” and hit ok. With “bouncycastle_lib” selected press Add External JARs and find the bcprov-jdk15on-165.jar. Apply and Close.
* Adding the User Library to our package: Right click your package in package explorer > Build path > Add Libraries. Select User libraries and then press next, now just tick “bouncycastle_lib” and click finish.
4. In the Noobchain class window right click and run the program.
