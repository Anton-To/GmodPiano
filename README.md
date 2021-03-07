# GmodPiano
A small java program i made to play midi files on a piano in Garry's mod\
Garry's mod plugin link https://steamcommunity.com/sharedfiles/filedetails/?id=104548572

This program can play midi files and also connect to take input from midi devices
![filemode](https://user-images.githubusercontent.com/18397628/110246748-812a5800-7f71-11eb-8c8a-19c8f8e1cce7.png)
![devicemode](https://user-images.githubusercontent.com/18397628/110246751-84254880-7f71-11eb-9c83-ba8b4f07126c.png)

Three numbers in the bottom are timings. Increasing them might make the program to perform better on servers with high latency.

t1 - Timing between pressing SHIFT and playing a note for notes that need to be played with SHIFT on.\
t2 - Timing between pressing a note and releasing shift for notes that need to be played with SHIFT on.\
t3 - Timing between pressing and releasing a note for notes that dont need SHIFT to be pressed.\

I'm not a professional programmer so plese dont mind the code quality.
