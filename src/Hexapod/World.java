package Hexapod;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.nio.FloatBuffer;

import javax.media.opengl.GL2;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class World
implements ActionListener {
	/* menus */
	JMenuItem buttonRescan, buttonDisconnect;
	
	/* world contents */
	Camera camera;
	Spidee robot0;
	
	boolean first_time=true;
	
	final int NUM_ROBOTS = 1;
	protected int activeRobot=0;

	
	public World() {
		camera = new Camera();
		robot0 = new Spidee("0");
	}
	

    protected void setup( GL2 gl2 ) {
		gl2.glDepthFunc(GL2.GL_LESS);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthMask(true);
    }

    
    public void update(float dt) {
    	camera.update(dt);
    	robot0.move(dt);
    }

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if(subject==buttonRescan) {
			robot0.DetectSerialPorts();
			//robot1.DetectSerialPorts();
			//TODO tell RobotTrainer to update all menus
			Hexapod.getSingleton().updateMenu();
			return;
		}
		if(subject==buttonDisconnect) {
			robot0.ClosePort();
			//robot1.ClosePort();
			Hexapod.getSingleton().updateMenu();
			return;
		}
	}
	
    public JMenu updateMenu() {
    	JMenu menu, subMenu;
        
        // connection menu
        menu = new JMenu("Connection(s)");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Connection settings.");
        
    	subMenu=robot0.getMenu();
        subMenu.setText("Spidee");
        menu.add(subMenu);

        buttonRescan = new JMenuItem("Rescan Ports",KeyEvent.VK_R);
        buttonRescan.getAccessibleContext().setAccessibleDescription("Rescan the available ports.");
        buttonRescan.addActionListener(this);
        menu.add(buttonRescan);

        menu.addSeparator();
        
        buttonDisconnect = new JMenuItem("Disconnect",KeyEvent.VK_D);
        buttonDisconnect.addActionListener(this);
        menu.add(buttonDisconnect);
        
        return menu;
    }
	
	public void render(GL2 gl2) {
		gl2.glEnable(GL2.GL_CULL_FACE);

		gl2.glPushMatrix();
			camera.render(gl2);

			gl2.glDisable(GL2.GL_CULL_FACE);

			//gl2.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);

			gl2.glDisable(GL2.GL_LIGHTING);
			PrimitiveSolids.drawGrid(gl2);
			 // Enable lighting
			gl2.glEnable(GL2.GL_LIGHTING);
			gl2.glEnable(GL2.GL_LIGHT0);
			gl2.glEnable(GL2.GL_COLOR_MATERIAL);
			
			FloatBuffer params = FloatBuffer.allocate(4);
			  params.put(0);
			  params.put(0);
			  params.put(0);
			  params.put(1);
			  params.rewind();
			  gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, params );
			  params.put(1);
			  params.put(1);
			  params.put(1);
			  params.put(1);
			  params.rewind();
			  gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, params );
			  gl2.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE );
			  gl2.glShadeModel(GL2.GL_SMOOTH);
			  gl2.glMateriali(GL2.GL_FRONT, GL2.GL_SHININESS, 96);
/*
			// set up lights
			FloatBuffer position = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    position.mark();
		    position.put(new float[] { -10f, 10f, 50f, 0f }); // even values about 10e3 for the first three parameters aren't changing anything
		    position.reset();
			gl2.glLight(GL2.GL_LIGHT0, GL2.GL_POSITION, position);

		    FloatBuffer ambient = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    ambient.mark();
		    ambient.put(new float[] { 0.85f, 0.85f, 0.85f, 1f });
		    ambient.reset();
		    gl2.glLight(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient);

		    FloatBuffer diffuse = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    diffuse.mark();
		    diffuse.put(new float[] { 1.0f, 1.0f, 1.0f, 1f });
		    diffuse.reset();
		    gl2.glLight(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse);
*/
			robot0.render(gl2);
			
		gl2.glPopMatrix();
	}
}
