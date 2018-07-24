
package beatbox;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

public class BeatBox {
JPanel background, mainPanel;
ArrayList<JCheckBox> checkboxList;
Sequencer sequencer;
Sequence  sequence;
Track track;
JFrame  theFrame;
JButton start,stop,upTempo,downTempo;
JCheckBox c;
JMenuBar menuBar;
JMenu menu;
JMenuItem saveIt, load;


String [] instrumentNames={"Bass Drum","Closed Hi-Hat","Open Hi-Hat",
    "Acoustic Snare","Crash Cymbal","Hand Clap","High Tom","Hi Bingo",
    "Maracas","Whistle","Low Conga","Cowbell","Vibraslap","Low-mid Tom",
    "High Agogo","Open Hi Conga"};

int [] instruments={35,42,46,
    38,49,39,50,60,
    70,72,64,56,58,47,
    67,63};

    public static void main(String[] args) {
new BeatBox().buildGUI();

    }
    
    public void buildGUI(){
        theFrame= new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        menuBar= new JMenuBar();
        menu=new JMenu("File");
        saveIt=new JMenuItem("Save");
        saveIt.addActionListener(new MySendListener());
       
        load=new JMenuItem("Load and play");
        load.addActionListener(new MyReadInListener());
        menu.add(load);
        menu.add(saveIt);
        menuBar.add(menu);
        theFrame.setJMenuBar(menuBar);
        
        BorderLayout layout= new BorderLayout();
        background=new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         background.setBackground(Color.YELLOW);
        checkboxList= new ArrayList<JCheckBox>();
        Box buttonBox= new Box(BoxLayout.Y_AXIS);
        //buttonBox.setBackground(Color.BLACK);
        
        start= new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        stop= new JButton("stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
         upTempo= new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
         downTempo= new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        Box nameBox= new Box(BoxLayout.Y_AXIS);
        for (int i=0;i<16;i++){
            nameBox.add(new Label(instrumentNames[i]));
        }
    
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        theFrame.getContentPane().add(background);
        
        GridLayout grid= new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        
        mainPanel=new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        mainPanel.setBackground(Color.red);
        for (int i=0; i<256; i++){
            c=new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
            
        }
        
            setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    }
    public void setUpMidi(){
        try {
            
            sequencer= MidiSystem.getSequencer();
            sequencer.open();
            sequence=new Sequence(Sequence.PPQ, 4);
            track=sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }
    catch(Exception ex){
        ex.printStackTrace();
    }
    }
    
    public void buildTrackAndStart(){
        int[] trackList=null;
        sequence.deleteTrack(track);
        track=sequence.createTrack();
        for(int i=0; i<16; i++){
            
            trackList=new int[16];
            int key=instruments[i];
            
            for (int j=0; j<16; j++){
                JCheckBox jc=(JCheckBox) checkboxList.get(j+(16*i));
                if (jc.isSelected()) {
                    trackList[j]=key;
                }
                else {
                    trackList[j]=0;
                }
                }
           makeTracks(trackList);
           track.add(makeEvent(176,1,127,0,16));
            
        }
           track.add(makeEvent(192,9,1,0,15));
           try {
               
               sequencer.setSequence(sequence);
               sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
               sequencer.start();
               sequencer.setTempoInBPM(120);
           }
        catch (Exception e){
            e.printStackTrace();
        }
        
        } 
    public class MyStartListener implements ActionListener {
       public void actionPerformed(ActionEvent a){
           buildTrackAndStart();
       } 
    }

     public class MyStopListener implements ActionListener {
       public void actionPerformed(ActionEvent a){
           sequencer.stop();
       } 
    }
    
     public class MyUpTempoListener implements ActionListener {
       public void actionPerformed(ActionEvent a){

           float tempoFactor=sequencer.getTempoFactor();
           sequencer.setTempoFactor((float) (tempoFactor*1.03));
       } 
    }
    
      public class MyDownTempoListener implements ActionListener {
       public void actionPerformed(ActionEvent a){
          float tempoFact=sequencer.getTempoFactor();
          sequencer.setTempoFactor((float)(tempoFact* .97));
       } 
    }
     
    
      public void makeTracks(int[] list){
          for (int i=0; i<16; i++){
              int key=list[i];
              if (key!=0){
                 track.add(makeEvent(144,9,key,100,i));
                 track.add(makeEvent(128,9,key,100,i+1));
              }
          }
      }
      
      public MidiEvent makeEvent(int comb, int chan, int one, int two, int tick){
          MidiEvent event=null;
          try {
              ShortMessage a= new ShortMessage();
              a.setMessage(comb,chan,one,two);
              event= new MidiEvent(a,tick);              
          } catch (Exception r){r.printStackTrace();}
          return event;
      }
      
    public class MySendListener implements ActionListener {
          public void actionPerformed(ActionEvent ev){
              JFileChooser al=new JFileChooser();
              al.showSaveDialog(theFrame);
              saveFile(al.getSelectedFile());
          }
      }
        
    public void saveFile(File file){
        boolean [] str= new boolean[256];
        for (int i=0; i<256; i++){
        JCheckBox box=(JCheckBox) checkboxList.get(i);
        if (box.isSelected()){
            str[i]=true;
        } else {
            str[i]=false;
        }
        
    }
    try {
        ObjectOutputStream fff=new ObjectOutputStream(new FileOutputStream(file));
        fff.writeObject(str);
        fff.close();
    } catch (Exception ex){ex.printStackTrace();}
    }
   
    
    
      public class MyReadInListener implements ActionListener {
          public void actionPerformed(ActionEvent ev){
              JFileChooser fi=new JFileChooser();
              fi.showOpenDialog(theFrame);
              loadFile(fi.getSelectedFile());
          }
      }
public void loadFile(File file){
    boolean [] checkBoxState=null;
              
              try{
                  ObjectInputStream in= new ObjectInputStream(new FileInputStream(file));
                  checkBoxState=(boolean []) in.readObject();
                  in.close();
                  
              } catch(Exception Ex){Ex.printStackTrace();}
              
              for (int i=0; i<256; i++){
                  JCheckBox check=(JCheckBox) checkboxList.get(i);
                  if (checkBoxState[i]){
                      check.setSelected(true);
                  } else {
                      check.setSelected(false);
                  }
              
              }
           
              sequencer.stop();
              buildTrackAndStart();
}
}