package com.sap.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.*;
import java.sql.Timestamp;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileWriter;

//uncommenting code on lines 261, 380 and 381 disables multithreading
@SpringBootApplication
public class DemoApplication {


    public static void main(String[] args)
    {
        // Thread Safe BlockingQueues are shared between consequetive threads 
        // to enable Transfer of Parts.

        BlockingQueue<Part> brake_machining_que = new ArrayBlockingQueue<Part>(1000), brake_quality_inspection_que = new ArrayBlockingQueue<Part>(1000),
                         brake_finishing_que = new ArrayBlockingQueue<Part>(1000), brake_packaging_que = new ArrayBlockingQueue<Part>(1000), 
                         brake_assembly_que = new ArrayBlockingQueue<Part>(1000);



        BlockingQueue<Part> tank_machining_que = new ArrayBlockingQueue<Part>(1000), tank_quality_inspection_que = new ArrayBlockingQueue<Part>(1000),
                         tank_finishing_que = new ArrayBlockingQueue<Part>(1000), tank_packaging_que = new ArrayBlockingQueue<Part>(1000), 
                         tank_assembly_que = new ArrayBlockingQueue<Part>(1000);
                         

        // First Element of start_time Vector holds the time when the first thread starts.

        Vector<Long> start_time = new Vector<Long>();


        // First element of if_outage Vector lets the other threads know if there is an outage at any given point of time.
        // Intitialised to False.

        Vector<Boolean> if_outage = new Vector<Boolean>();
        if_outage.add(false);
        long outage_start_time=15000;
        long outage_duration=0;


        // Producer Thread -> reads from input.json and waits till materials are procured (i.e 10 milliseconds) and adds to the
        // brake_machining_que after materials are procured.

        Producer brake_material_procurement = new Producer("brakes","material_procurement", 
                                                            brake_machining_que,10, start_time, if_outage);


        // Processor Thread for Machining and Forming of Brakes:
        // Fecthes Brake from brake_machining Que and executes machining and forming for 15 milliseconds,
        // post which the Brake is added into brake_quality_inspection_que.
                                
        Processor brake_machining = new Processor("brakes","machining_and_forming",brake_machining_que, 
                                                            brake_quality_inspection_que,15, start_time, if_outage);
        Processor brake_quality_inspection = new Processor("brakes","quality_inspection",brake_quality_inspection_que,
                                                            brake_finishing_que,20, start_time, if_outage);
        Processor brake_finishing = new Processor("brakes","finishing_and_quoting",brake_finishing_que,
                                                            brake_packaging_que,25, start_time, if_outage);
        
        // Once the packaging and storage of a given Brake is completed it is added into brake_assembly_que
        // ready for assembly.
        Processor brake_packaging = new Processor("brakes","packaging_and_storage",brake_packaging_que,
                                                            brake_assembly_que,30, start_time, if_outage);

  

        Thread brake_material_procurement_thread = new Thread(brake_material_procurement);
        Thread brake_machining_thread = new Thread(brake_machining);
        Thread brake_quality_inspection_thread = new Thread(brake_quality_inspection);
        Thread brake_finishing_thread = new Thread(brake_finishing);
        Thread brake_packaging_thread = new Thread(brake_packaging);
  

        //All Threads w.r.t to Brake are started
        brake_material_procurement_thread.start();
        brake_machining_thread.start();
        brake_quality_inspection_thread.start();
        brake_finishing_thread.start();
        brake_packaging_thread.start();


  
        // Tank Threads similar to Brake Threads.

        Producer tank_material_procurement = new Producer("tanks","material_procurement", tank_machining_que,15, start_time, if_outage);
        Processor tank_machining = new Processor("tanks","machining_and_forming",tank_machining_que, tank_quality_inspection_que,28, start_time, if_outage);
        Processor tank_quality_inspection = new Processor("tanks","quality_inspection",tank_quality_inspection_que,tank_finishing_que,30, start_time, if_outage);
        Processor tank_finishing = new Processor("tanks","finishing_and_quoting",tank_finishing_que,tank_packaging_que,35, start_time, if_outage);
        Processor tank_packaging = new Processor("tanks","packaging_and_storage",tank_packaging_que,tank_assembly_que,40, start_time, if_outage);

  

        Thread tank_material_procurement_thread = new Thread(tank_material_procurement);
        Thread tank_machining_thread = new Thread(tank_machining);
        Thread tank_quality_inspection_thread = new Thread(tank_quality_inspection);
        Thread tank_finishing_thread = new Thread(tank_finishing);
        Thread tank_packaging_thread = new Thread(tank_packaging);
  

        tank_material_procurement_thread.start();
        tank_machining_thread.start();
        tank_quality_inspection_thread.start();
        tank_finishing_thread.start();
        tank_packaging_thread.start();


        // Assembler Thread Takes 1 Brake from brake_assembly_que and 1 tank from tank_assembly_que
        // and thereby assembles it in assembling_time_per_unit i.e. 10 milliseconds
    
        Assembler assembly = new Assembler(brake_assembly_que, tank_assembly_que, 10, start_time, if_outage);
        Thread assembly_thread = new Thread(assembly);
        assembly_thread.start();

        
        // Outage Thread is created with if_outage, outage_start_time: milliseconds after which outage should start,
        // outage_duration: duration of outage in millliseconds.
        
        Outage outage = new Outage(if_outage, outage_start_time, outage_duration);
        Thread outage_thread = new Thread(outage);
        outage_thread.start();

    }
}
  
class Producer implements Runnable {
  

    String part_type;
    String process_name;
    BlockingQueue<Part> output_que;
    int processing_time_per_unit;
    Vector<Long> start_time;
    Vector<Boolean> if_outage;
  
    public Producer(String part_type, String process_name, BlockingQueue<Part> output_que,
                        int processing_time_per_unit, Vector<Long> start_time, Vector<Boolean> if_outage)
    {
        this.part_type = part_type;
        this.process_name = process_name;
        this.output_que = output_que;
        this.start_time = start_time;
        this.processing_time_per_unit = processing_time_per_unit;
        this.if_outage =if_outage;
    }


    // Fetches input of type part_type from input.json and returns Part[] array.

    private Part[] fetch_input(){
        
        JSONParser jsonParser = new JSONParser();
        Part[] parts_array = null;
         
        try (FileReader reader = new FileReader("src/main/java/com/sap/demo/input.json"))
        {
            //Read JSON file
            JSONObject  obj = (JSONObject) jsonParser.parse(reader);
 
            JSONArray parts_list = (JSONArray) obj.get(part_type);

            parts_array = new Part[parts_list.size()];

            for(int i=0;i<parts_list.size();i++){
                JSONObject part = (JSONObject)parts_list.get(i);
                parts_array[i] = new Brake((String)part.get("part_id") , (String)part.get("part_number"));
            }


 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } 

        return parts_array;

    }
  
    @Override public void run()
    {   
        // Logging Start time 
        start_time.add(System.currentTimeMillis());

        Part[] parts_array = fetch_input();
        for (int i = 0; i < parts_array.length; i++) {
            Part part = parts_array[i];

            //Execution gets stuck in this loop till if_outage[0] is false.

            while(if_outage.get(0));

            try {
                part.assembly_status = "awaiting_" +process_name;
                part.status_time_stamp = new Timestamp(System.currentTimeMillis());

                // process in progress
                Thread.sleep(processing_time_per_unit);

                part.assembly_status = process_name+"_completed";
                part.status_time_stamp = new Timestamp(System.currentTimeMillis());

                output_que.put(part);

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end_time = System.currentTimeMillis();
        System.out.println("["+part_type.toUpperCase()+"] "+process_name+ " completed at: "+String.valueOf(end_time-start_time.get(0)) +" milliseconds.");
        System.out.println();
    }
}
  
class Processor implements Runnable {
    
    String part_type;
    String process_name;
    BlockingQueue<Part> input_que;
    BlockingQueue<Part> output_que;
    int processing_time_per_unit;
    Vector<Long> start_time;
    Vector<Boolean> if_outage;
  
    Part taken = null;
  
    public Processor(String part_type, String process_name, BlockingQueue<Part> input_que, BlockingQueue<Part> output_que, 
                        int processing_time_per_unit, Vector<Long> start_time, Vector<Boolean> if_outage)
    {
        this.part_type = part_type;
        this.process_name = process_name;
        this.input_que = input_que;
        this.output_que = output_que;
        this.start_time = start_time;
        this.processing_time_per_unit =processing_time_per_unit;
        this.if_outage =if_outage;
    }
  
    @Override public void run()
    {   
        String part_number;
        int number = 0;

	// Uncommenting below code disables Multithreading
        //while(input_que.size()!=999);

        // Thread keeps looping until we process the last part whose part_number is 999.
        while (number != 999) {

            // Outage loop
            while(if_outage.get(0));

            try {

                taken = input_que.take();

                taken.assembly_status = "awaiting_"+process_name;
                taken.status_time_stamp = new Timestamp(System.currentTimeMillis());

                //Process in Progress
                Thread.sleep(processing_time_per_unit);

                taken.assembly_status = process_name+"_completed";
                taken.status_time_stamp = new Timestamp(System.currentTimeMillis());

                output_que.put(taken);

                part_number = taken.part_number;
                number = Integer.parseInt(part_number.split("-")[1]);


            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long end_time = System.currentTimeMillis();
        System.out.println("["+part_type.toUpperCase()+"] "+process_name+ " completed at: "+String.valueOf(end_time-start_time.get(0)) +" milliseconds.");
        System.out.println();
    }
}


class Assembler implements Runnable {
  

    BlockingQueue<Part> brake_assembly_que;
    BlockingQueue<Part> tank_assembly_que;
    Vector<MotorCycle> assembled_motorcycles;
    int assembling_time_per_unit;
    Vector<Long> start_time;
    Vector<Boolean> if_outage;

    Part tank = null;
    Part brake = null;
  
    public Assembler(BlockingQueue<Part> brake_assembly_que, BlockingQueue<Part> tank_assembly_que, 
                        int assembling_time_per_unit, Vector<Long> start_time, Vector<Boolean> if_outage)
    {
        this.brake_assembly_que = brake_assembly_que;
        this.tank_assembly_que = tank_assembly_que;
        this.assembled_motorcycles = new Vector<MotorCycle>();
        this.assembling_time_per_unit = assembling_time_per_unit;
        this.start_time = start_time;
        this.if_outage =if_outage;

    }

    private void saveOutput(){
        // Saves the motorcycle Array into output.json after the assembly ends.

        JSONArray moto_array = new JSONArray();
        for(MotorCycle m: assembled_motorcycles){
            JSONObject moto = new JSONObject();
 
            JSONArray brakes_array = new JSONArray();
            JSONArray tanks_array = new JSONArray();
            JSONObject brake = new JSONObject();
            JSONObject tank = new JSONObject();

            brake.put("part_id", m.brake.part_id);
            brake.put("part_number", m.brake.part_number);
            brake.put("assembly_status", m.brake.assembly_status);
            brake.put("status_timestamp", m.brake.status_time_stamp.toString());
            brakes_array.add(brake);

            tank.put("part_id", m.tank.part_id);
            tank.put("part_number", m.tank.part_number);
            tank.put("assembly_status", m.tank.assembly_status);
            tank.put("status_timestamp", m.tank.status_time_stamp.toString());
            tanks_array.add(tank);

            moto.put("motorcycle_number", m.motorcycle_number);
            moto.put("assembly_status", m.assembly_status);
            moto.put("brakes", brakes_array);
            moto.put("tanks", tanks_array);

            moto_array.add(moto);

        }
        JSONObject main = new JSONObject();
        main.put("motorcycle_assembly", moto_array);

    try {
         FileWriter file = new FileWriter("src/main/java/com/sap/demo/output.json");
         file.write(main.toJSONString());
         file.close();
      } catch (IOException e) {

         e.printStackTrace();
      }


    }

  
    @Override public void run()
    {   
    
        int number =0;

	// Uncommenting below code disables Multithreading
        // while(brake_assembly_que.size()!=999);
        // while(tank_assembly_que.size()!=999);
    
        // Loops until last motorcycle with motorcycle_number: 999 has been assembled.
        while (number!=999) {

            //outage loop
            while(if_outage.get(0));

            try {

                // Assembly happens only when both brake_assembly_que and tank_assembly_que
                // are non Empty.
                if(!brake_assembly_que.isEmpty() && !tank_assembly_que.isEmpty()){

                    brake = brake_assembly_que.take();
                    tank = tank_assembly_que.take();
                    number++;

                    if(number==1){
						System.out.println();
                         System.out.println("---- Assembly Started ------");
                        System.out.println();
                    }

                    MotorCycle motorCycle = new MotorCycle(brake,tank);

                    //Assembly in Progress
                    Thread.sleep(assembling_time_per_unit);

                    motorCycle.assembly_status = "motorcycle_assembly_completed";
                    motorCycle.motorcycle_number = "MOTO-"+String.format("%03d", number);
                    assembled_motorcycles.add(motorCycle);

                }

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end_time = System.currentTimeMillis();
        System.out.println("Motorcycles assembly completed at: "+String.valueOf(end_time-start_time.get(0)) +" milliseconds.");
        System.out.println();

        saveOutput();

    }
}


class Outage implements Runnable {
  

    Vector<Boolean> if_outage;
    long outage_start_time;
    long outage_duration;
  
    public Outage(Vector<Boolean> if_outage, long outage_start_time, long outage_duration)
    {
        this.if_outage = if_outage;
        this.outage_start_time = outage_start_time;
        this.outage_duration = outage_duration;


    }

    @Override public void run()
    {   try{
                // outage thread is made to sleep for outage_start_time milliseconds.
                Thread.sleep(outage_start_time);

				if(outage_duration!=0){
					System.out.println();
					System.out.println("Outage Started");

					// all concurrent threads who have access to if_outage vector halt their
					// production tasks till Outage ends.

					if_outage.add(0,true);
					Thread.sleep(outage_duration);

					System.out.println();
					System.out.println("Outage Ended");
					System.out.println();
		
					if_outage.add(0,false);
				}

                // production tasks are resumed.

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


    }

}


class Part{
    final String part_id;
    final String part_number;
    String assembly_status;
    Timestamp status_time_stamp;

    public Part(String part_id, String part_number){
        this.part_id = part_id;
        this.part_number = part_number;
        this.assembly_status = "awaiting_for_material_delivery";
        this.status_time_stamp = new Timestamp(System.currentTimeMillis());
    }
    

}

class Brake extends Part{
    
    public Brake(String part_id, String part_number){
        super(part_id, part_number);
    }

}

class Tank extends Part{
    
    public Tank(String part_id, String part_number){
        super(part_id, part_number);
    }

}

class MotorCycle {
    Part brake;
    Part tank;
    String assembly_status;
    String motorcycle_number;

    public MotorCycle(Part brake, Part tank){
        this.brake = brake;
        this.tank = tank;
        this.assembly_status = "awaiting_assembly";

    }

}
