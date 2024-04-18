package DataGenerator;

import java.io.*;
import java.util.*;
public class DataGenerator {
    Random random = new Random();
    int ncars, nxway;
    int total_qid = 0;
    BufferedWriter writer;
    public DataGenerator(String outputFilePath, int ncars, int nxway) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFilePath, false));
        this.ncars = ncars;
        this.nxway = nxway;
    }
    private void reportPosition(int time, Cars car)throws IOException{
        int qid = 0;
        int qtype = 0;
        int q_start = 0, q_end = 0;
        int q_dow = 0, q_m = 0, q_day = 0;
        if(random.nextInt(100) == 0) {
            int rand = random.nextInt(100);
            if (rand < 50) { // account balance
                qtype = 2;
            } else if (rand < 60) { // daily expenditure
                qtype = 3;
                q_dow = random.nextInt(7) + 1;
                q_m = random.nextInt(1440) + 1;
                q_day = random.nextInt(69) + 1;
            } else { // travel time prediction
                qtype = 4;
                q_start = random.nextInt(100);
                q_end = random.nextInt(100);
            }
        }

        String outputString = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d", qtype,
                time,
                car.id,
                car.speed,
                car.xway,
                car.lane,
                car.dir,
                car.pos/5280,
                car.pos,
                time,
                qid,
                q_start,
                q_end,
                q_dow,
                q_m,
                q_day
        );
        writer.write(outputString);
        writer.newLine();
    }
    private void run() throws IOException{
        Map<Integer, Cars> cars = new HashMap<>();
        Set<Integer> runningCarsId = new HashSet<>();
        Map<Integer, List<Integer>> startingCars = new HashMap<>();
        for(int i = 0; i < ncars; i++){
            int cid = random.nextInt(1000000);
            while(cars.containsKey(cid)){
                cid = random.nextInt(1000000);
            }
            int enter_time = random.nextInt(300);
            int xway = random.nextInt(nxway);
            int dir = random.nextInt(2);
            int lane = random.nextInt(3) + 1;
            int speed = random.nextInt(40) + 1;
            int enter_seg = random.nextInt(100);
            int pos = random.nextInt(5280) + enter_seg * 5280;
            int dest_seg;
            if(dir == 0){
                if(enter_seg == 99){
                    enter_seg = 98;
                }
                dest_seg = random.nextInt(100 - enter_seg - 1) + 1 + enter_seg;
            }else{
                if(enter_seg == 0){
                    enter_seg = 1;
                }
                dest_seg = enter_seg - 1 - random.nextInt(enter_seg);
            }
            if(!startingCars.containsKey(enter_time)){
                startingCars.put(enter_time, new LinkedList<>());
            }
            cars.put(cid, new Cars(cid, speed, xway, pos, lane, dir, dest_seg, enter_time));
            startingCars.get(enter_time).add(cid);
        }
        int startTime = 0;
        int endTime = 3 * 60 * 60;
        int accidentInterval = 20 * 60;
        Set<Integer> accidentCars = new HashSet<>();
        int accidentEndTime = 0;
        System.out.println("Start simulation.");
        long start_time = System.currentTimeMillis();
        for(int ctime = startTime; ctime <= endTime; ctime ++){
            int reportNum = 0;
            // Accident
            if(ctime == accidentEndTime){
                accidentCars.clear();
            }
            if(ctime > accidentEndTime && ctime % accidentInterval == 0){
                accidentEndTime = ctime + random.nextInt(10 * 60) + 10 * 60;
                int accidentCarNum = random.nextInt(runningCarsId.size() - 1) + 2;
                ArrayList<Integer> shuffledCarIds = new ArrayList<>(runningCarsId);
                for(int i = 0; i < shuffledCarIds.size(); i++){
                    int j = random.nextInt(shuffledCarIds.size());
                    int t = shuffledCarIds.get(i);
                    shuffledCarIds.set(i, shuffledCarIds.get(j));
                    shuffledCarIds.set(j, t);
                }
                for(int i = 0; i < accidentCarNum; i++){
                    accidentCars.add(shuffledCarIds.get(i));
                }
            }

            // Car running
            List<Integer> removeCars = new LinkedList<>();
            for(int cid: runningCarsId){
                Cars car = cars.get(cid);
                int new_pos = car.pos;
                if(accidentCars.contains(cid)) {
                    if (car.dir == 0) {
                        new_pos += (int) Math.ceil(car.speed * 1.4666666);
                        if (new_pos > 527999) {
                            new_pos = 527999;
                        }
                    } else {
                        new_pos -= (int) Math.ceil(car.speed * 1.4666666);
                        if (new_pos < 0) {
                            new_pos = 0;
                        }
                    }
                    // Check reach end
                    if (car.dir == 0 && new_pos >= (car.dest_seg + 1) * 5280 - 1 || car.dir == 1 && new_pos <= car.dest_seg * 5280) {
                        removeCars.add(cid);
                    } else {
                        int new_seg = new_pos / 5280;
                        int new_speed = random.nextInt(60) + 40;
                        int new_lane = random.nextInt(3) + 1;
                        if (new_seg == car.dest_seg) {
                            new_speed = random.nextInt(40) + 1;
                            new_lane = 0;
                        }
                        car.speed = new_speed;
                        car.pos = new_pos;
                        car.lane = new_lane;
                        // report
                        if ((car.enter_time - ctime) % 30 == 0) {
                            reportPosition(ctime, car);
                            reportNum ++;
                        }
                    }
                }else{
                    car.speed = 0;
                    if ((car.enter_time - ctime) % 30 == 0) {
                        reportPosition(ctime, car);
                        reportNum ++;
                    }
                }
            }
            // Remove cars
            for(int carId: removeCars){
                runningCarsId.remove(carId);
                Cars car = cars.get(carId);
                int enter_time = ctime + random.nextInt(300) + 300;
                int xway = random.nextInt(nxway);
                int dir = random.nextInt(2);
                int lane = random.nextInt(3) + 1;
                int speed = random.nextInt(40) + 1;
                int enter_seg = random.nextInt(100);
                int pos = random.nextInt(5280) + enter_seg * 5280;
                int dest_seg;
                if(dir == 0){
                    if(enter_seg == 99){
                        enter_seg = 98;
                    }
                    dest_seg = random.nextInt(100 - enter_seg - 1) + 1 + enter_seg;
                }else{
                    if(enter_seg == 0){
                        enter_seg = 1;
                    }
                    dest_seg = enter_seg - 1 - random.nextInt(enter_seg);
                }
                car.xway = xway;
                car.dir = dir;
                car.lane = lane;
                car.speed = speed;
                car.pos = pos;
                car.dest_seg = dest_seg;
                if(!startingCars.containsKey(enter_time)){
                    startingCars.put(enter_time, new LinkedList<>());
                }
                startingCars.get(enter_time).add(carId);
            }

            // Add cars
            if(startingCars.containsKey(ctime)) {
                for (int carId : startingCars.get(ctime)) {
                    runningCarsId.add(carId);
                    Cars car = cars.get(carId);
                    reportPosition(ctime, car);
                    reportNum++;
                }
                startingCars.get(ctime).clear();
                startingCars.remove(ctime);
            }
            System.out.println("Running car: " + runningCarsId.size() + " Reports per second: " + reportNum);
        }
        writer.close();
    };
    public static void main(String[] args)throws IOException{
        String fileName = args[0];
        System.out.println(args[0]);
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        String outputDir = prop.getProperty("output_directory");
        int ncars = 50000, nxway = 10;
        DataGenerator dataGenerator = new DataGenerator(outputDir + "/generatedRecords.txt", ncars, nxway);
        dataGenerator.run();
    }
}