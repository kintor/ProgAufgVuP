#Abschlussprojekt zur Vorlesung „Verteilte und Parallele Systeme“
In der Vorlesung haben Sie das Chord-System, eine Distributed Hash Table
(DHT), kennengelernt. Im Rahmen des Abschlussprojektes sollen Sie eine
Chord-DHT (in Java oder C) selbst implementieren. Abgabe des Quelltextes,
sowie eines lauffähigen Programms bis zum 14.7.2013. Für die Zulassung zur
Prüfung ist das Abschlussprojekt obligatorisch notwendig. Folgende
Funktionalität soll realisiert werden:

1. Ein einfaches Chord-System, welches per Zufall erzeugte und interaktiv
   eingegebene Daten speichert, welche dann über die DHT gesucht werden
   können. Hierzu ist eine Hashfunktion, der Knotenbeitritt, sowie das
   Stabilization-Protokoll erforderlich. Eine Finger-Tabelle und das
   Maskieren von Knotenausfällen sind nicht notwendig. Für die
   Kommunikation zwischen den Knoten sollen TCP-Verbindungen verwendet
   werden.
2. Das Programm soll vier Parameter auf der Konsole entgegen nehmen:
	- ip: die eigene IP-Adresse
	- port: der eigene Port
	- cip: die IP-Adresse eines bereits teilnehmenden Knotens
	- cport: der Port eines bereits teilnehmenden Knotens Der erste Knoten
	  wird lediglich mit den beiden Parametern (-ip und -port) gestartet,
	  bei allen weiteren Knoten müssen beim Start zusätzlich die beiden
	  Parameter (-cip und -cport) angegeben werden.
3. Es soll möglich sein, das Programm auf einem physikalischen Rechner
   mehrfach auszuführen, sodass ein komplettes Chord-System auf einem
   Rechner simuliert werden kann. Erstellen Sie zwei Skripte, welche 4
   respektive 8 Knoten auf einem Rechner automatisch starten. Dies
   erleichtert Ihnen das Testen und uns die Abnahme am Ende.
4. Ferner soll es eine textbasierte Schnittstelle zum interaktiven Testen
   des Chord-Systems geben. Diese soll jeder Knoten, neben sinnvollen
   Ereignisausgaben, anbieten. Die Schnittstelle soll drei Befehle zur
   Verfügung stellen:

	| Befehl | Wert | Beschreibung |
	-------- | ---- | ------------ |
	__put__ | *value* | Speichert die Zeichenkette value im Chord-System |
	Ausgabe: | hash | Hash-Wert unter dem die Daten gespeichert wurden |
	| | node | Knoten (ID, IP und Port) auf dem Daten gespeichert wurden |
	__get__ | *hash* | Sucht die Daten (Zeichenkette) zu dem gegebenen Hashwert.
	Ausgabe: | value | Zeichenkette des gesuchten Hash-Wertes.
	| | node | Knoten (ID, IP und Port) von dem Daten gelesen wurden.
	__list__ | | Gibt alle lokal gespeicherten Daten mit Hashwerten aus.
	Ausgabe: | values | Die Daten.


Der Quelltext und das lauffähige Programm müssen bis zum 14.07.2013,
23.59Uhr per Mail an Herrn Florian Klein geschickt werden. Spätere Abgaben
können nicht berücksichtigt werden! Zusätzlich muss das lauffähige Programm
in der letzten Übungsstunde, am 18.07.2013, demonstriert werden. Die genaue
Uhrzeit der jeweiligen Demonstration wird nach Abgabe aller Programme
bekannt gegeben. Es ist möglich das Abschlussprojekt paarweise zu
bearbeiten. In diesem Fall muss allerdings klar erkennbar sein, wer welche
Arbeiten erledigt hat (beide Namen im Kopf jeder Quellcodedatei ist nicht
ausreichend!). Außerdem muss vor Beginn des Abschlussprojektes bei Herrn
Florian Klein angemeldet werden, welche beiden Studenten zusammen arbeiten
werden.
