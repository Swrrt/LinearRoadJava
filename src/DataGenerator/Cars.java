package DataGenerator;

public class Cars {
    int id; // car identifier
    int speed; // mile per hour (0~100)
    int xway; // expressway id
    int pos; // feet (0~527999)
    int lane; // lane number
    int dir; // direction (0 or 1)
    int dest_seg; // will exit in the segment (0~99)
    int enter_time; // time enter expressway
    Cars(int id, int speed, int xway, int pos, int lane, int dir, int dest_seg, int enter_time){
        this.id = id;
        this.speed = speed;
        this.xway = xway;
        this.pos = pos;
        this.lane = lane;
        this.dir = dir;
        this.dest_seg = dest_seg;
        this.enter_time = enter_time;
    }
}
