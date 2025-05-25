import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.util.*;

public class PROJECT extends JFrame {
    JTextField nameF, customDoseF;
    JSpinner hS, mS;
    JComboBox<String> ampmC, freqC, doseC;
    JPanel medsP;
    JLabel nextL;
    java.util.List<Med> meds = new ArrayList<>();
    Med editing = null;
    JButton addB;
    javax.swing.Timer t;
    String[] doses = {"1 Tablet", "2 Tablets", "5 ml", "10 ml", "Custom"};
    String[] freqs = {"Daily", "Alternate Days", "Weekly", "Every 6 Hours", "Every 8 Hours", "One-time", "Custom"};
    public PROJECT() {
        setTitle("Medication Tracker");
        setDefaultCloseOperation(3);
        setSize(700, 800);
        setLocationRelativeTo(null);
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel form = new JPanel(new GridBagLayout());
        TitledBorder fTitle = BorderFactory.createTitledBorder("Add New Medication");
        fTitle.setTitleFont(new Font("SansSerif", 1, 20));
        form.setBorder(fTitle);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;
        Font fnt = new Font("SansSerif", 0, 16);
        // Name
        g.gridx=0;g.gridy=0;JLabel nameL=new JLabel("Medicine Name:");nameL.setFont(fnt);form.add(nameL,g);
        g.gridx=1;nameF=new JTextField(20);nameF.setFont(fnt);nameF.setPreferredSize(new Dimension(220,32));form.add(nameF,g);
        // Dosage
        g.gridx=0;g.gridy=1;JLabel doseL=new JLabel("Dosage:");doseL.setFont(fnt);form.add(doseL,g);
        g.gridx=1;JPanel doseP=new JPanel(new FlowLayout(0,0,0));doseC=new JComboBox<>(doses);doseC.setFont(fnt);
        doseC.setPreferredSize(new Dimension(120,32));customDoseF=new JTextField(15);customDoseF.setFont(fnt);
        customDoseF.setPreferredSize(new Dimension(120,32));customDoseF.setVisible(false);doseC.addActionListener(e->
        {customDoseF.setVisible("Custom".equals(doseC.getSelectedItem()));doseP.revalidate();doseP.repaint();});
        doseP.add(doseC);doseP.add(Box.createHorizontalStrut(5));
        doseP.add(customDoseF);form.add(doseP,g);
        // Time
        g.gridx=0;g.gridy=2;JLabel timeL=new JLabel("Time:");timeL.setFont(fnt);form.add(timeL,g);
        g.gridx=1;JPanel timeP=new JPanel(new FlowLayout(0,0,0));
        hS=new JSpinner(new SpinnerNumberModel(8,1,12,1));
        mS=new JSpinner(new SpinnerNumberModel(0,0,59,1));
        hS.setFont(fnt);mS.setFont(fnt);((JSpinner.DefaultEditor)hS.getEditor()).getTextField().
        setFont(fnt);((JSpinner.DefaultEditor)mS.getEditor()).getTextField().setFont(fnt);hS.setPreferredSize(new Dimension(50,32));
        mS.setPreferredSize(new Dimension(50,32));ampmC=new JComboBox<>(new String[]{"AM","PM"});
        ampmC.setFont(fnt);ampmC.setPreferredSize(new Dimension(70,
        32));timeP.add(hS);timeP.add(new JLabel(":"));timeP.add(mS);
        timeP.add(Box.createHorizontalStrut(5));timeP.add(ampmC);form.add(timeP,g);
        // Frequency
        g.gridx=0;g.gridy=3;JLabel freqL=new JLabel("Frequency:");freqL.setFont(fnt);form.add(freqL,g);
        g.gridx=1;freqC=new JComboBox<>(freqs);freqC.setFont(fnt);freqC.setPreferredSize(new Dimension(220,32));form.add(freqC,g);
        // Add Button
        g.gridx=0;g.gridy=4;g.gridwidth=2;addB=new JButton("Add Medication");addB.setFont(new Font("SansSerif",1,18));
        addB.setPreferredSize
        (new Dimension(350,38));addB.addActionListener(e->addOrEdit());form.add(addB,g);
        g.gridx=0;g.gridy=5;form.add(Box.createRigidArea(new Dimension(0,5)),g);
        // Next Reminder
        nextL=new JLabel("Next Reminder: None");nextL.setFont(nextL.getFont().deriveFont(nextL.getFont().getSize()+1f));
        nextL.setHorizontalAlignment(SwingConstants.CENTER);
        nextL.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel top=new JPanel();top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));top.add(form);top.add(Box.createVerticalStrut(10));
        top.add(nextL);
        // Medications Panel
        medsP=new JPanel();medsP.setLayout(new BoxLayout(medsP,BoxLayout.Y_AXIS));
        TitledBorder mTitle=BorderFactory.createTitledBorder("Added Medications");
        mTitle.setTitleFont(new Font("SansSerif",1,20));medsP.setBorder(mTitle);medsP.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane scroll=new JScrollPane(medsP);scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        main.add(top,BorderLayout.PAGE_START);main.add(scroll,BorderLayout.CENTER);add(main);
        t=new javax.swing.Timer(60000,e->{updateNext();notifyUser();});t.start();
        updateMeds();updateNext();
    }
    void addOrEdit() {
        String n=nameF.getText().trim();
        String d="Custom".equals(doseC.getSelectedItem())?customDoseF.getText().trim():(String)doseC.getSelectedItem();
        if(n.isEmpty()||d.isEmpty())return;
        int h=(int)hS.getValue(),m=(int)mS.getValue();
        String ap=(String)ampmC.getSelectedItem(),fq=(String)freqC.getSelectedItem();
        if(editing==null)meds.add(new Med(n,d,h,m,ap,fq));
        else{editing.n=n;editing.d=d;
            editing.h=h;
            editing.m=m;
            editing.ap=ap;
            editing.fq=fq;editing=null;addB.setText("Add Medication");
        }
        updateMeds();
        updateNext();
        clearF();
    }
    void updateMeds() {
        medsP.removeAll();
        for(Med m:meds)medsP.add(card(m));
        medsP.revalidate();medsP.repaint();
    }
    JPanel card(Med m) {
        JPanel c=new JPanel(new BorderLayout());c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY),
        BorderFactory.createEmptyBorder(20,20,20,20)));
        JPanel info=new JPanel();info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));Font f=new Font("SansSerif",0,22);
        JLabel nL=new JLabel("Medicine: "+m.n),dL=new JLabel("Dosage: "+m.d),tL=new JLabel("Time: "+m.tS()),fL=new JLabel("Frequency: "+m.fq);
        for(JLabel l:new JLabel[]{nL,dL,tL,fL}){l.setFont(f);l.setAlignmentX(Component.LEFT_ALIGNMENT);info.add(l);
            info.add(Box.createVerticalStrut(4));}
        c.add(info,BorderLayout.CENTER);
        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton eB=new JButton("Edit"),delB=new JButton("Delete");
        eB.addActionListener(e->{editing=m;nameF.setText(m.n);boolean p=false;for(int i=0;i<doseC.getItemCount();i++)
        if(m.d.equals(doseC.getItemAt(i))){doseC.setSelectedItem(m.d);customDoseF.setVisible(false);p=true;break;
        }if(!p){doseC.setSelectedItem("Custom");
        customDoseF.setText(m.d);customDoseF.setVisible(true);}((JPanel)customDoseF.getParent()).revalidate();
        ((JPanel)customDoseF.getParent()).repaint();
        int hr=m.t().getHour();String ap="AM";if(hr>=12){ap="PM";if(hr>12)hr-=12;}if(hr==0)hr=12;hS.setValue(hr);mS.setValue(m.t().getMinute());
        ampmC.setSelectedItem(ap);
        freqC.setSelectedItem(m.fq);addB.setText("Save Changes");}); delB.addActionListener(e->{meds.remove(m);updateMeds();updateNext();});
        btnP.add(eB);btnP.add(delB);c.add(btnP,BorderLayout.SOUTH);
        int minH=170;c.setPreferredSize(new Dimension(c.getPreferredSize().width,minH));c.setMaximumSize(new Dimension(Integer.MAX_VALUE,minH));
        c.setMinimumSize(new Dimension(c.getPreferredSize().width,minH));c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
    void clearF(){
        nameF.setText("");
        doseC.setSelectedItem(doses[0]);customDoseF.setText("");
        customDoseF.setVisible(false);
        ((JPanel)customDoseF.getParent()).revalidate();
        ((JPanel)customDoseF.getParent()).repaint();hS.setValue(8);mS.setValue(0);ampmC.setSelectedItem("AM");
        freqC.setSelectedItem(freqs[0]);
    editing=null;addB.setText("Add Medication");
}
    void updateNext(){
        if(meds.isEmpty()){nextL.setText("Next Reminder: None");return;
    }

    LocalTime now=LocalTime.now();
    Med next=null;LocalTime nextT=null;
    for(Med m:meds){LocalTime t=m.t();
        if(t.isAfter(now)){if(nextT==null||t.isBefore(nextT)){nextT=t;next=m;}}}if(next!=null)nextL.setText("Next Reminder: "+next.n+" at "+next.tS());
    else nextL.setText("Next Reminder: None for today");}
    void notifyUser(){LocalTime now=LocalTime.now().withSecond(0).withNano(0);
        for(Med m:meds)if(m.t().equals(now))JOptionPane.
        showMessageDialog(this,"It's time to take your medication: "+m.n+"\nDosage: "+m.d,"Medication Reminder",JOptionPane.INFORMATION_MESSAGE);}
    public static void main(String[]a){SwingUtilities.invokeLater(()->new PROJECT().setVisible(true));}
    static class Med{
        String n,d,ap,fq;int h,m;
        Med(String n,String d,int h,int m,String ap,String fq){
            this.n=n;
            this.d=d;
            this.h=h;
            this.m=m;
            this.ap=ap;
            this.fq=fq;
        }
        LocalTime t(){int h24=h;
            if("PM".equals(ap)&&h!=12)h24+=12;
            if("AM".equals(ap)&&h==12)h24=0;
            return LocalTime.of(h24,m);}
            String tS(){return String.format("%02d:%02d %s",h,m,ap);
        }
    }
} 

