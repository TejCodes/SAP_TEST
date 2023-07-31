## SAP_TEST

Please Use the following commmand to run the Application:

./mvnw spring-boot:run

Note: To run the code without multithreading, please comment out the while loop code in the lines 261 (Processor class), 380 and 381 (Assembler class).

Results of the Problem statement are visible in the command line.

## Input:
Demo application takes input.json as input, which basically has 999 brakes and 999 tanks ( with unique part_id and part_number ), 
which need to be produced and thereby assembled to manufacture a motorcycle.

## **Output:**
Under the assumption that, each Motorcycle only requires 1 brake system and 1 fuel tank system, output.json is generated which contains
the list of assembled motorcycles and their respective assembly status.


## **Setting Outage parameters:**

Outage_start_time: Number of milliseconds after which the outage should start.

Outage_duration: Number of milliseconds the outage should last. 

Please SET outage_duration =0, to disable outage functionality.


## **RESULTS:**

## **TESTCASE 1:**

### Multithreading enabled - without outage:

---- Assembly Started ------

[BRAKES] material_procurement completed at: 11044 milliseconds.

[TANKS] material_procurement completed at: 16106 milliseconds.

[BRAKES] machining_and_forming completed at: 16127 milliseconds.

[BRAKES] quality_inspection completed at: 21536 milliseconds.

[BRAKES] finishing_and_quoting completed at: 26639 milliseconds.

[TANKS] machining_and_forming completed at: 29380 milliseconds.

[TANKS] quality_inspection completed at: 31411 milliseconds.

[BRAKES] packaging_and_storage completed at: 31537 milliseconds.

[TANKS] finishing_and_quoting completed at: 36394 milliseconds.

[TANKS] packaging_and_storage completed at: 42367 milliseconds.

Motorcycles assembly completed at: 42385 milliseconds.





### Multithreading enabled - with outage:

outage_start_time=15000; // outage starts at 15,000 miliiseconds.

outage_duration=15000;  // outage lasts till 30,000 milliseconds.

---- Assembly Started ------

[BRAKES] material_procurement completed at: 11568 milliseconds.


Outage Started

Outage Ended

[BRAKES] machining_and_forming completed at: 32219 milliseconds.

[TANKS] material_procurement completed at: 32312 milliseconds.

[BRAKES] quality_inspection completed at: 42028 milliseconds.

[BRAKES] finishing_and_quoting completed at: 44943 milliseconds.

[TANKS] machining_and_forming completed at: 46374 milliseconds.

[TANKS] quality_inspection completed at: 47988 milliseconds.

[BRAKES] packaging_and_storage completed at: 48052 milliseconds.

[TANKS] finishing_and_quoting completed at: 58234 milliseconds.

[TANKS] packaging_and_storage completed at: 60842 milliseconds.

Motorcycles assembly completed at: 60858 milliseconds.

As we can see from the results, during the uutage period all the production and assembly queues were halted.
All the queues were back to functioning post the Outage, and we can see that overall assembly time is
larger than when there was no outage.


## **Test Case 2:**

### With Multithreading:

---- Assembly Started ------

[BRAKES] material_procurement completed at: 11044 milliseconds.

[TANKS] material_procurement completed at: 16106 milliseconds.

[BRAKES] machining_and_forming completed at: 16127 milliseconds.

[BRAKES] quality_inspection completed at: 21536 milliseconds.

[BRAKES] finishing_and_quoting completed at: 26639 milliseconds.

[TANKS] machining_and_forming completed at: 29380 milliseconds.

[TANKS] quality_inspection completed at: 31411 milliseconds.

[BRAKES] packaging_and_storage completed at: 31537 milliseconds.

[TANKS] finishing_and_quoting completed at: 36394 milliseconds.

[TANKS] packaging_and_storage completed at: 42367 milliseconds.

Motorcycles assembly completed at: 42385 milliseconds.

We can see from the above results that, assembly started pretty early on, which is in a indication that the moment 1st brake and 1st tank 
have completed packing_and_storage stage, the assembly has begun immediately.




### Without MultiThreading:

To simulate the case without multithreading, every production task was only started after its queue was completely full (i.e. 999 items).
For example, machining_and_forming was started only after the machining_and_forming_que was full.

Also, Assembler only starts assembling after both the tank_assembly_que and brake_assembly_que were full in order to simulate non-multithreading case.

To run the code without multithreading, please comment out the while loop code in the lines 261 (Processor class), 380 and 381 (Assembler class).

[BRAKES] material_procurement completed at: 11433 milliseconds.

[TANKS] material_procurement completed at: 16213 milliseconds.

[BRAKES] machining_and_forming completed at: 27604 milliseconds.

[TANKS] machining_and_forming completed at: 45910 milliseconds.

[BRAKES] quality_inspection completed at: 50888 milliseconds.

[TANKS] quality_inspection completed at: 77408 milliseconds.

[BRAKES] finishing_and_quoting completed at: 77416 milliseconds.

[BRAKES] packaging_and_storage completed at: 108943 milliseconds.

[TANKS] finishing_and_quoting completed at: 113869 milliseconds.

[TANKS] packaging_and_storage completed at: 161469 milliseconds.

---- Assembly Started ------

Motorcycles assembly completed at: 177176 milliseconds.

As we can see, without Multithreading, the assembly only started after the brake and tank assembly queues were full.
We can also see that, without Multithreading the system took a lot more time than otherwise.








