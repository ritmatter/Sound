Sound
=====

A project that records simple melodies and scores them

This project currently uses FTP methods supplied in JTransforms, written by 
Piotr Wendykier.

This project also uses graphing from JFreeChart, and it takes some information
for using Java Sound from Matthias Pfisterer.  

COMPILE MULTIPLE PITCH READ : 

FROM THE pitchTracking DIRECTORY :
javac -cp "../../*" *.java -d ../classes/

USE MULTIPLE PITCH READ : 

FROM THE src DIRECTORY :
java -cp "../*:./classes" pitchTracking.MultiplePitchRead <soundFile> <graphTitle>