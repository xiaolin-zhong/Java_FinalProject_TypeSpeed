Summer 2022
This is Java Final Project.

The requirement for completion is to incorporate at least 3 "advanced concepts" learned in class.

1.	To run this, please run Run.java. This will open 2 UI, one serving as a client and one serving as a server. 
2.	Click on the Connect button on client UI to connect to the server. Once connection is established, you’ll be able to press the start button. 
3.	Once Start is pressed, a single word will appear at a time in the Word section of the server UI. In the client UI, enter the word that is shown on the Word section of the server UI in the Type Here section of the client UI. After you’ve typed the word correctly, press enter. If your input is correct, the next word will appear in the server UI. This will last for 60 seconds; the time left will appear on the server UI. 
The client will send the user’s input to the server. The server will determine whether the input is correct. If it is correct, next word is shown, and the Type Here section will clear, allowing the user to enter the next word.
4.	Once a minute has passed, no more words will appear on the server, and you will be unable to enter any more inputs in the client. Your resulting words per minute (WPM) will be shown. 
5.	The client will connect to the “leaderboard.db” and insert the latest WPM attempt. In the client UI, there is an option on the top to look at the leaderboard. ID will continue to increment at every attempt. The ID serves as the primary key of this table. Score is the WPM received on that attempt. The leaderboard is sorted by the in descending order by WPM. 
6.	When quit is pressed, both the client and server will exit.
