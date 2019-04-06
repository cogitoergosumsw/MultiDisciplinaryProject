#include <SharpIR.h>
#include <PinChangeInterrupt.h>
#include <RunningMedian.h>
#include <DualVNH5019MotorShield.h>

#define L_ENCODER 3 //left motor encoder A to pin 3
#define R_ENCODER 11 //right motor encoder A to pin 11

DualVNH5019MotorShield md;

/*Creating instance for sensors*/
SharpIR sensorFC(SharpIR::GP2Y0A21YK0F, A0); //Front Centre
SharpIR sensorFL(SharpIR::GP2Y0A21YK0F, A1); //Front Left
SharpIR sensorFR(SharpIR::GP2Y0A21YK0F, A2); //Front Right
SharpIR sensorLF(SharpIR::GP2Y0A21YK0F, A3); //Left Front
SharpIR sensorLB(SharpIR::GP2Y0A21YK0F, A4); //Left Back
SharpIR sensorRight(SharpIR::GP2Y0A02YK0F, A5); //Right

/**Constructor for median*/
static RunningMedian FrontCenter = RunningMedian(50);
static RunningMedian FrontLeft = RunningMedian(50);
static RunningMedian FrontRight = RunningMedian(50);
static RunningMedian LeftFront = RunningMedian(50);
static RunningMedian LeftBack = RunningMedian(50);
static RunningMedian LongRight = RunningMedian(100);

boolean dataComplete = false;

float disFL, disFC, disFR, disLF, disLB, disRight;
double dFL, dFC, dFR, dLF, dLB, dR;
double prevLF;
String FL, FC, FR, LF, LB, R;
String whatyouwant ="";

double disFrontLeft,disFrontCenter, disFrontRight, disLeftFront, disLeftBack, disLongRight;
char rpiString[60];
int count=0;
int a=0;
volatile int L_EncTick = 0;
volatile int R_EncTick = 0;

void setup()
{
  Serial.begin(115200);
  md.init();
  pinMode(L_ENCODER,INPUT);
  pinMode(R_ENCODER,INPUT);
  attachPCINT(digitalPinToPCINT(L_ENCODER), &L_EncoderInc, RISING);
  attachPCINT(digitalPinToPCINT(R_ENCODER), &R_EncoderInc, RISING);
  Serial.setTimeout(80); //for rpi serial msg
}

void loop()
{
     if(Serial.available()>0){     
        count =0 ;
        for( int i = 0; i < sizeof(rpiString);  ++i ){
          rpiString[i] = (char)0;
        }
          while(Serial.available()>0){ 
            char dStr= Serial.read();
            rpiString[count] = dStr;
            if (rpiString[count] != ""){
            rpiInput(rpiString[count]);
            }
            count++;
           }
     }    
}

void rpiInput(char cmd){

  switch(cmd){
    case 'S': //Send sensor data
      case 's': 
      printSensorData();
      break;
      
    case 'F' :
      mForward(267);  //forward 1 grid exploration
       printSensorData();
      break;
      
    case 'f' :
      mfastForward(283); //forward 1 grid fastest path
      break;  
  
    case 'L' :    
      turnLeft(372); //turn left 90 exploration
      //checkCalibrate();
      printSensorData();
      break;
      
    case 'l':
      delay(30);     
      turnLeft(374); //turn left 90 fastest path
      delay(100);
      break;
  
    case 'R' :
      turnRight(372);//turn right 90
      // checkCalibrate();
      printSensorData();
      break;
      
    case 'r':
      delay(30);
      turnRight(372);//turn right 90
      delay(100);
      break;
      
    case 'C':
      case 'c':
      checkCalibrate();
     break;  

    case 'Q':
      case'q':
       adjustLeft();
       break;
  }
}
/**Exploration move forward**/
void mForward(int endTick){
  L_EncTick=0; R_EncTick = 0;
  double output=0;
  int m1Speed=350, m2Speed=350;
    while(L_EncTick < endTick){
     output = pidControl(L_EncTick,R_EncTick);
     md.setSpeeds(m1Speed + output ,m2Speed);    
    }
    brakes();
    adjustLeft();
    delay(80);    
}
/**Fastest path move forward**/
void mfastForward(int endTick){
  L_EncTick=0; R_EncTick = 0;
  double output=0;
  int m1Speed=350, m2Speed=350;
    while(L_EncTick < endTick){
     output = pidControl(L_EncTick,R_EncTick);
     md.setSpeeds(m1Speed + output ,m2Speed);    
    }
    brakes();
}

/**Left Turn**/
void turnLeft(int endTick){
  L_EncTick =0; R_EncTick = 0;
  double output=0;
  int i =-350, j=350;
   while(L_EncTick < endTick){      
      output = pidControl(L_EncTick,R_EncTick);
      md.setSpeeds(i -output ,j );
  }
 
 brakes();
 delay(100); 
}

/**Right Turn**/
void turnRight(int endTick){
  L_EncTick =0; R_EncTick = 0;
  double output=0;
  int i = 350, j = -350;
  while(R_EncTick < endTick){  
      output = pidControl(L_EncTick,R_EncTick);
      md.setSpeeds(i + output ,j );
  } 
  brakes();
  delay(100); 
}
void brakes(){
    md.setBrakes(400, 400);
}

/*********************************************************************
 *                            PID                                    *
 **********************************************************************/
int pidControl(int LeftPos, int RightPos) {

  int err;
  double output;
  double Kp = 2.92;// batt1: 4
  //double Kd = 0;
 // double Ki = 0;

  err = LeftPos - RightPos;
  output = Kp * err;
  return output;
}

/*********************************************************************
 *                            SENSORS                                *
 **********************************************************************/
 //read short range sensors data
double readSensor(SharpIR sensor, double offset){
  double dis;
  dis = sensor.getDistance() + offset;
  return dis;
}

void getAllSensor(){
  //sample odd times for short range sensors
  for (int sCount = 0; sCount < 69 ; sCount++)
  {
    //Calculate the distance in centimeters and store the value in a variable
    disFL = readSensor(sensorFL, -3);//-2
    disFC = readSensor(sensorFC, -3);//-3
    disFR = readSensor(sensorFR, -3);//-1
    disLF = readSensor(sensorLF, -5);//-5
    disLB = readSensor(sensorLB, -2.4);//-2
    disRight = readSensor(sensorRight, -6);//3 
    //add the variables into arrays as samples
    FrontLeft.add(disFL);
    FrontCenter.add(disFC);
    FrontRight.add(disFR);
    LeftFront.add(disLF);
    LeftBack.add(disLB);
    LongRight.add(disRight);
  }
  dFL = FrontLeft.getMedian() ;
  dFC = FrontCenter.getMedian() ;
  dFR = FrontRight.getMedian() ;
  dLF = LeftFront.getMedian() ;
  dLB = LeftBack.getMedian() ;
  dR = LongRight.getMedian();
}

void printSensorData(){
  String whatyouwant = "SENSOR_DATA|";  
  getAllSensor();
  //frontLeft
   if(dFL > 4 and dFL < 9){ whatyouwant = whatyouwant+ "1,"; }
  else if(dFL > 8 and dFL < 16){ whatyouwant = whatyouwant+ "2,"; }
    else if (dFL > 15 and dFL < 27){ whatyouwant = whatyouwant + "3,"; }
      else if( dFL > 26 and dFL < 39){ whatyouwant = whatyouwant +"4,"; }
      else{ whatyouwant = whatyouwant + "0,"; }
       
   //frontCenter
   if(dFC > 4 and dFC < 9){ whatyouwant = whatyouwant+ "1,"; }
    else if(dFC > 8 and dFC < 16){ whatyouwant = whatyouwant + "2,"; }
    else if (dFC > 15 and dFC < 27){ whatyouwant = whatyouwant + "3,"; }
      else if( dFC > 26 and dFC <40){ whatyouwant = whatyouwant + "4,"; }
      else{ whatyouwant = whatyouwant + "0,"; }
      
    //frontRight
  if(dFR > 4 and dFR < 9){ whatyouwant = whatyouwant + "1,"; }
   else if(dFR > 8 and dFR < 18){ whatyouwant = whatyouwant + "2,"; }
    else if (dFR > 17 and dFR < 27){ whatyouwant = whatyouwant + "3,";}
      else if( dFR > 26 and dFR < 33){ whatyouwant = whatyouwant + "4,"; }
      else { whatyouwant = whatyouwant + "0,"; }
      
  //leftFront
  if(dLF > 0 and dLF < 9){ whatyouwant = whatyouwant + "1,";}
   else if(dLF > 8 and dLF < 18){ whatyouwant = whatyouwant + "2,"; }
    else if (dLF > 17 and dLF < 30){ whatyouwant = whatyouwant +  "3,"; }
      else if( dLF > 29 and dLF < 41){ whatyouwant = whatyouwant + "4,"; }
      else{ whatyouwant = whatyouwant + "0,"; }
      
   //leftBack
  if(dLB > 0 and dLB < 9){ whatyouwant = whatyouwant +  "1,"; }
   else if(dLB > 8 and dLB < 18){ whatyouwant = whatyouwant + "2,"; }
    else if (dLB > 17 and dLB < 31){ whatyouwant = whatyouwant + "3,"; }
      else if( dLB > 30 and dLB < 49){ whatyouwant = whatyouwant + "4,"; }
      else{ whatyouwant = whatyouwant + "0,";  }
      
      //LongRight
   if(dR > 4 and dR <= 14){ whatyouwant = whatyouwant +  "1"; }
      else if( dR > 14 and dR < 21){ whatyouwant = whatyouwant + "2"; }
      else if( dR > 20 and dR < 28){ whatyouwant = whatyouwant + "3"; }
      else if( dR > 27 and dR < 38){ whatyouwant = whatyouwant + "4"; }
      else if( dR >37){ whatyouwant = whatyouwant + "0"; }
      else{ whatyouwant = whatyouwant + "0"; }
      
  clearSensorMedian();
    Serial.println(whatyouwant + ";");
    Serial.flush();
}
void clearSensorMedian(){
  FrontLeft.clear() ;
  FrontCenter.clear();
  FrontRight.clear();
  LeftFront.clear();
  LeftBack.clear();
  LongRight.clear();
}
/***********************************************
 *   Calibration + Calibration motor movement  *
 ***********************************************/

void aFront_F(int endTick){
  L_EncTick =0; R_EncTick = 0;
  int left = 100;  int right = 100 ;
  int output =0;
  while(L_EncTick < endTick){
     output = pidControl(L_EncTick,R_EncTick);
     md.setSpeeds(left + output ,right);    
    }
  Calibrakes();
}
void aFront_B(int endTick){
  L_EncTick =0; R_EncTick = 0;
  int left = -100;  int right = -100 ;
  int output =0;
  while(L_EncTick < endTick){
     output = pidControl(L_EncTick,R_EncTick);
     md.setSpeeds(left - output ,right);    
    }
  Calibrakes();
}
void aLeft_L(int endTick){
  L_EncTick =0; R_EncTick = 0;
  int left = -100;  int right = 100 ;
  int output=0;
  while(L_EncTick < endTick){
     output = pidControl(L_EncTick,R_EncTick);
     md.setSpeeds(left + output ,right);    
    }
  Calibrakes();
}
void aLeft_R(int endTick){
  L_EncTick =0; R_EncTick = 0;
  int left = 100;  int right = -100 ;

  while(R_EncTick < endTick){  
      md.setSpeeds(left ,right);
  }
  Calibrakes();
}
void Calibrakes(){
    md.setBrakes(30, 30);
}
void getSensorCalibrate(){
    for (int sCount = 0; sCount < 59 ; sCount++)
  {
    //Calculate the distance in centimeters and store the value in a variable
    disFL = readSensor(sensorFL, -3);
    disFC = readSensor(sensorFC, -3);
    disFR = readSensor(sensorFR, -3);
    disLF = readSensor(sensorLF, -5);
    disLB = readSensor(sensorLB, -2.5);
    //add the variables into arrays (59 samples)
    FrontLeft.add(disFL);
    FrontCenter.add(disFC);
    FrontRight.add(disFR);
    LeftFront.add(disLF);
    LeftBack.add(disLB);
  }
  dFL = FrontLeft.getMedian() ;
  dFC = FrontCenter.getMedian() ;
  dFR = FrontRight.getMedian() ;
  dLF = LeftFront.getMedian() ;
  dLB = LeftBack.getMedian() ;
}

void checkCalibrate(){
  getSensorCalibrate();  
    /***Calibrate with any 2 front walls***/
    if (dFL > 0 or dFC > 0 or dFR > 0 or dLF > 0){
          if((dFL < 12 and dFR < 12) or (dFL < 12 and dFC < 12) or (dFC < 12 and dFR < 12)) {
                /***if there is no left wall  or only left front wall***/
                if((dLF > 11 or dLB > 11) or (dLF < 7 and (dLB < 0 or dLB > 6))) {
                  frontWallCalibrate();
                }
                /***if there is two left walls - above 5.5 and less than 8.5***/
                else if((dLF >5.5 and dLB > 5.5) and (dLF < 8.5 and (dLB > 0 and dLB < 8.5))){
                  frontWallCalibrate();
                 adjustLeft();
                }
                /***if there is two left walls - above 0 and below 6 ***/
                else if( dLF < 6 and (dLB > 0 and dLB < 6 )){
                  turnLeft(372);
                  delay(100);
                  frontWallCalibrate();
                  turnRight(372); 
                  delay(100);
                  frontWallCalibrate();
                }
           }
           /***Calibrate without front walls***/
           else if((dFL > 11 and dFC > 11) or (dFL > 11 and dFR > 11) or (dFC > 11 and dFR > 11)){
                 /***if there is two left walls - above 4.5 and less than 12***/
                if((dLF > 4.5 and dLB > 4.5) and (dLF < 12 and dLB < 12)){
                  adjustLeft();
                }
                /***if there is two left walls - above 0 and below 4.5 or more than 7 less than 14***/
                else if(( dLF < 4.6 and (dLB > 0 and dLB < 4.6)or(dLF>7 or dLB >7))and(dLF<14 or dLB < 14)){
                  turnLeft(374);
                  delay(30);
                  frontWallCalibrate();
                  delay(50);
                  turnRight(374); 
                  delay(30);
                }
           }
    }
  clearSensorMedian();
}

void frontWallCalibrate(){
  adjustDistance();
  adjustFront();
}

//Front distance calibration
void adjustDistance(){
  while(1){
    getSensorCalibrate();
    if(((dFL > 0 and dFL < 5.5 ) and(dFC > 0 and dFC < 5.5 ))
        or ((dFC > 0 and dFC < 5.5 ) and (dFR > 0 and dFR < 5.5 ))
          or((dFL > 0 and dFL < 5.5 ) and (dFR > 0 and dFR < 5.5 ))){
             aFront_B(20);
           }
    else if(((dFL > 6.5 and dFL < 12) and(dFC > 6.5 and dFC < 12))
            or ((dFC > 6.5 and dFC < 12) and (dFR > 6.5 and dFR < 12))
              or ((dFL > 6.5 and dFL < 12) and (dFR > 6.5 and dFR < 12))){
            aFront_F(20);
      
           }
    else { break; } 
    clearSensorMedian();
  }
}

//Front angle calibration
void adjustFront(){
  while(1){
    getSensorCalibrate();
    double errFL = dFL - 9;
    double errFR = dFR - 9;
    double errFC = dFC - 9;
    
    double errDiff_FLFR = errFL - errFR;
    double errDiff_FLFC = errFL - errFC;
    double errDiff_FCFR = errFC - errFR;
    
    if((dFL > 4  and dFL < 12) and (dFR >4 and dFR < 12)){
      if(abs(errDiff_FLFR) < 0.3 ){ break; }
      else if (errFL > errFR ) { aLeft_R(20); }
      else if(errFR > errFL){ aLeft_L(20); }
    }
    else if((dFL > 4  and dFL < 12) and (dFC >4 and dFC < 12)){
       if(abs(errDiff_FLFC) < 0.3 ){ break; }
      else if (errFL > errFC ) { aLeft_R(20);}
      else if(errFC > errFL){ aLeft_L(20); }
    }
    else if((dFC > 4  and dFC < 12) and (dFR >4 and dFR < 12)){
       if(abs(errDiff_FCFR) < 0.3 ){ break; }
      else if (errFC > errFR ) { aLeft_R(20); }
      else if(errFR > errFC){ aLeft_L(20); }
    }
    else { break; }
    clearSensorMedian();
  }
}
//Left wall calibration
void adjustLeft(){
  while(1){
    getSensorCalibrate();
    double errLF = dLF - 9;
    double errLB = dLB - 9;

    double errDiff_LFLB = errLF - errLB;

    if((dLF > 4  and dLF < 9) and (dLB > 4 and dLB < 9)){
      if(abs(errDiff_LFLB) < 0.3){ break; }
      else if (abs(errLF > errLB)) {  aLeft_L(5); }
      else if(abs(errLB > errLF)){ aLeft_R(5); }
    }
    else { break; }
    clearSensorMedian();
  }
}

/**Encoder Interrupt Tick Inc**/
void L_EncoderInc(void){  L_EncTick++;  }

void R_EncoderInc(void){  R_EncTick++;  }
