# SAP_TEST

Please Use the Following commmand to run the Application:
./mvnw spring-boot:run

Results of the Problem Statement are visible in the command line.

Input:
Demo application takes input.json as input, which basically has 999 brakes and 999 tanks ( with unique part_id and part_number ), 
which need to be produced and thereby assembled to manufacture a motorcycle.

Output:
Under the assumption that, each Motorcycle only requires 1 Brake system and 1 Fuel Tank system, Output.json is generated which contains
the list of assembled motorcycles and their respective assembly status.


Setting Outage parameters:

outage_start_time: Number of Milliseconds after which the outage should start.
outage_duration: Number of Milliseconds the Outage should last. 

Please SET outage_duration =0, to disable outage functionality.

RESULTS:


