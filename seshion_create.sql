-- The results of running this script will be spooled
-- to 'spoolCreate.txt'

\o '/srv/seshion/application/spoolCreate.txt'

-- Output script execution data
\qecho -n 'Script run on '
\qecho -n `date /t`
\qecho -n 'at '
\qecho `time /t`
\qecho -n 'Script run by ' :USER ' on server ' :HOST ' with db ' :DBNAME
\qecho ' '
\qecho

\qecho Creating table UserAccount
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserAccount
(
 Username VARCHAR(30) NOT NULL PRIMARY KEY
	 CHECK (CHAR_LENGTH(TRIM(Username)) >= 6 AND CHAR_LENGTH(TRIM(Username)) <= 30),
 Password TEXT NOT NULL
	 CHECK (CHAR_LENGTH(TRIM(Password)) >= 0),
 Salt BYTEA NOT NULL,
 isVisibilityPrivate BOOLEAN NOT NULL,
 Latitude REAL,
 Longitude REAL,
 isOnline BOOLEAN NOT NULL,
 Description VARCHAR(100)
);

\qecho ' '
\qecho ' '
\qecho Creating table FriendsWith
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS FriendsWith
(theUser VARCHAR(30) REFERENCES UserAccount,
 Friend VARCHAR(30) REFERENCES UserAccount 
 	CHECK (Friend <> theUser),
 isFriendRequestAccepted BOOLEAN NOT NULL,
 CONSTRAINT friendswith_pk PRIMARY KEY(theUser, Friend)
);

\qecho ' '
\qecho ' '
\qecho Creating table UserGroup
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserGroup
(GID UUID NOT NULL PRIMARY KEY,
 Name VARCHAR(50) NOT NULL
 	CHECK (CHAR_LENGTH(TRIM(Name)) > 0),
 Owner VARCHAR(30) REFERENCES UserAccount
);

\qecho ' '
\qecho ' '
\qecho Creating table UserGroup_GroupMember
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserGroup_GroupMember
(GID UUID REFERENCES UserGroup ON DELETE CASCADE,
 UserMember VARCHAR(30) NOT NULL
	CHECK (CHAR_LENGTH(TRIM(UserMember)) >= 6 AND CHAR_LENGTH(TRIM(UserMember)) <= 30),
 CONSTRAINT groupmember_pk PRIMARY KEY(GID, UserMember)
);

\qecho ' '
\qecho ' '
\qecho Creating table Message
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS Message
(MID UUID NOT NULL PRIMARY KEY,
 MessageContent VARCHAR(250) NOT NULL
 	CHECK (CHAR_LENGTH(TRIM(MessageContent)) > 0),
 isPrivateMessage BOOLEAN NOT NULL,
 isGroupMessage BOOLEAN NOT NULL,
 Creator VARCHAR(30) REFERENCES UserAccount,
 DateCreated DATE NOT NULL,
 TimeCreated TIME NOT NULL
);

\qecho ' '
\qecho ' '
\qecho Creating table Message_Recipient
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS Message_Recipient
(MID UUID REFERENCES Message,
 Recipient VARCHAR(30) NOT NULL
	CHECK (CHAR_LENGTH(TRIM(Recipient)) >= 6 AND CHAR_LENGTH(TRIM(Recipient)) <= 30),
 CONSTRAINT msgrecipient_pk PRIMARY KEY(MID, Recipient)
);

\qecho ' '
\qecho ' '
\qecho Creating table UserSession
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserSession
(SID UUID NOT NULL PRIMARY KEY,
 Name VARCHAR(50) NOT NULL
 	CHECK (CHAR_LENGTH(TRIM(Name)) > 0),
 Owner VARCHAR(30) REFERENCES UserAccount,
 Description VARCHAR(300),
 LatitudeTopLeft REAL NOT NULL,
 LongitudeTopLeft REAL NOT NULL,
 LatitudeTopRight REAL NOT NULL,
 LongitudeTopRight REAL NOT NULL,
 LatitudeBottomLeft REAL NOT NULL,
 LongitudeBottomLeft REAL NOT NULL,
 LatitudeBottomRight REAL NOT NULL,
 LongitudeBottomRight REAL NOT NULL,
 StartDate DATE NOT NULL,
 EndDate DATE
 	CHECK (EndDate >= StartDate),
 StartTime TIME NOT NULL,
 EndTime TIME,
 isPrivate BOOLEAN NOT NULL,
 hasEnded BOOLEAN NOT NULL
);

\qecho ' '
\qecho ' '
\qecho Creating table UserSession_InvitedUser
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserSession_InvitedUser
(SID UUID REFERENCES UserSession ON DELETE CASCADE,
 theUser VARCHAR(30) NOT NULL
	CHECK (CHAR_LENGTH(TRIM(theUser)) >= 6 AND CHAR_LENGTH(TRIM(theUser)) <= 30),
 CONSTRAINT session_invited_pk PRIMARY KEY(SID, theUser)
);

\qecho ' '
\qecho ' '
\qecho Creating table Session_ShowedUpUser
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS UserSession_ShowedUpUser
(SID UUID REFERENCES UserSession ON DELETE CASCADE,
 theUser VARCHAR(30) NOT NULL
	CHECK (CHAR_LENGTH(TRIM(theUser)) >= 6 AND CHAR_LENGTH(TRIM(theUser)) <= 30),
 CONSTRAINT session_showedup_pk PRIMARY KEY(SID, theUser)
);

\qecho ' '
\qecho ' '
\qecho Creating table Contains
\qecho ----------------------------

CREATE TABLE IF NOT EXISTS Contains
(SID UUID REFERENCES UserSession ON DELETE CASCADE,
 theUser VARCHAR(30) REFERENCES UserAccount,
 CONSTRAINT contains_pk PRIMARY KEY(SID, theUser)
);

-- Turn off spooling
