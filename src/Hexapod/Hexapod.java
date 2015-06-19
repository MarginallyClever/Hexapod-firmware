package Hexapod;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.vecmath.Vector3f;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPipelineFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.Animator;


public class Hexapod 
implements ActionListener, GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
	static final long serialVersionUID=1;
	static final String version="1";
    static Hexapod __singleton;

	World world;

	/** menus */
	JMenuBar mainMenu;
	JMenuItem buttonAbout, buttonCheckForUpdate;
	JMenuItem buttonQuit;
	
	/* window management */
    final JFrame frame; 
	private GLJPanel glcanvas;
    private JTabbedPane contextMenu;
    private Splitter split_left_right;
    private JPanel cameraPanel=null, spideePanel=null;

	private JButton buttonFlyUp;
	private JButton buttonFlyDown;
	private JButton buttonFlyLeft;
	private JButton buttonFlyRight;
	private JButton buttonFlyForward;
	private JButton buttonFlyBackward;
	
	private JButton buttonLookUp;
	private JButton buttonLookDown;
	private JButton buttonLookLeft;
	private JButton buttonLookRight;
	
	private JButton buttonSpideeStand;
	private JButton buttonSpideeSit;
	
	private JButton buttonSpideeUp;
	private JButton buttonSpideeDown;
	
	private JButton buttonSpideeStill;
	private JButton buttonSpideeWalkStyle1;
	private JButton buttonSpideeWalkStyle2;
	private JButton buttonSpideeWalkStyle3;
	
	private JButton buttonSpideeWalkForward;
	private JButton buttonSpideeWalkBack;
	private JButton buttonSpideeWalkLeft;
	private JButton buttonSpideeWalkRight;
	private JButton buttonSpideeTurnLeft;
	private JButton buttonSpideeTurnRight;

    /* animation system */
    final Animator animator = new Animator();
    
    /* timing for animations */
    long start_time;
    long last_time;
    

	// settings
	private Preferences prefs;
	private String[] recentFiles = {"","","","","","","","","",""};

	
	public static void main(String[] argv) {
		getSingleton();
	}
	
	
	static public Hexapod getSingleton() {
		if(__singleton==null) __singleton = new Hexapod();
		return __singleton;
	}
	
	
	public JFrame GetMainFrame() {
		return frame;
	}
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	private void createCameraPanel() {
		cameraPanel = new JPanel(new GridLayout(0,1));
		
		cameraPanel.add(buttonFlyUp = createButton("fly up"));
		cameraPanel.add(buttonFlyDown = createButton("fly down"));
		cameraPanel.add(buttonFlyLeft = createButton("fly left"));
		cameraPanel.add(buttonFlyRight = createButton("fly right"));
		cameraPanel.add(buttonFlyForward = createButton("fly forward"));
		cameraPanel.add(buttonFlyBackward = createButton("fly backward"));

		cameraPanel.add(buttonLookUp = createButton("look up"));
		cameraPanel.add(buttonLookDown = createButton("look down"));
		cameraPanel.add(buttonLookLeft = createButton("look left"));
		cameraPanel.add(buttonLookRight = createButton("look right"));
	}
	
	private void createSpideePanel() {
		spideePanel = new JPanel(new GridLayout(0,1));
		
		spideePanel.add(buttonSpideeStand = createButton("stand"));
		spideePanel.add(buttonSpideeSit = createButton("sit"));

		spideePanel.add(buttonSpideeUp = createButton("body up"));
		spideePanel.add(buttonSpideeDown = createButton("body down"));

		spideePanel.add(buttonSpideeStill = createButton("stand still"));
		spideePanel.add(buttonSpideeWalkStyle1 = createButton("walk style 1"));
		spideePanel.add(buttonSpideeWalkStyle2 = createButton("walk style 2"));
		spideePanel.add(buttonSpideeWalkStyle3 = createButton("walk style 3"));

		spideePanel.add(buttonSpideeWalkForward = createButton("walk forward"));
		spideePanel.add(buttonSpideeWalkBack = createButton("walk backward"));
		spideePanel.add(buttonSpideeWalkLeft = createButton("walk left"));
		spideePanel.add(buttonSpideeWalkRight = createButton("walk right"));
		spideePanel.add(buttonSpideeTurnLeft = createButton("turn left"));
		spideePanel.add(buttonSpideeTurnRight = createButton("turn right"));
	}
	
	protected Hexapod() {
		prefs = Preferences.userRoot().node("Hexapod");
		
		LoadConfig();
		
        frame = new JFrame( "Hexapod SPIDEE-1" ); 
        frame.setSize( 1024, 768 );
        frame.setLayout(new java.awt.BorderLayout());


        world = new World();
        mainMenu = new JMenuBar();
        updateMenu();
        frame.setJMenuBar(mainMenu);

        
        final Animator animator = new Animator();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
              // Run this on another thread than the AWT event queue to
              // make sure the call to Animator.stop() completes before
              // exiting
              new Thread(new Runnable() {
                  public void run() {
                    animator.stop();
                    System.exit(0);
                  }
                }).start();
            }
          });

        glcanvas = new GLJPanel();
        
        animator.add(glcanvas);
        glcanvas.addGLEventListener(this);
        
        createCameraPanel();
        createSpideePanel();

        contextMenu = new JTabbedPane();
        contextMenu.addTab("Camera",null,cameraPanel,null);
        contextMenu.addTab("SPIDEE",null,spideePanel,null);

        split_left_right = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        split_left_right.add(glcanvas);
        split_left_right.add(contextMenu);

		//frame.setFocusable(true);
		//frame.requestFocusInWindow();
        //frame.addKeyListener(this);
        //frame.addMouseListener(this);
        //frame.addMouseMotionListener(this);
/*
		// focus not returning after modal dialog boxes
		// http://stackoverflow.com/questions/5150964/java-keylistener-does-not-listen-after-regaining-focus
		frame.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent e){
                //System.out.println("Focus GAINED:"+e);
            }
            public void focusLost(FocusEvent e){
                //System.out.println("Focus LOST:"+e);

                // FIX FOR GNOME/XWIN FOCUS BUG
                e.getComponent().requestFocus();
            }
        });
*/
        frame.add(split_left_right);
        frame.validate();
        frame.setVisible(true);
        
        animator.start();
        
        last_time = start_time = System.currentTimeMillis();
    }
	
	
	public void updateMenu() {
		mainMenu.removeAll();
		
        JMenu menu = new JMenu("Hexapod");
        
	        buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
	        buttonAbout.getAccessibleContext().setAccessibleDescription("About this program");
	        buttonAbout.addActionListener(this);
	        menu.add(buttonAbout);
	        
	        buttonCheckForUpdate = new JMenuItem("Check for update",KeyEvent.VK_A);
	        buttonCheckForUpdate.addActionListener(this);
	        menu.add(buttonCheckForUpdate);
	        
	        buttonQuit = new JMenuItem("Quit");
	        buttonQuit.getAccessibleContext().setAccessibleDescription("Goodbye...");
	        buttonQuit.addActionListener(this);
	        menu.add(buttonQuit);
        
        mainMenu.add(menu);
        
        mainMenu.add(world.updateMenu());
        
        mainMenu.updateUI();
	}
	
	
	public void CheckForUpdate() {
		try {
		    // Get Github info?
			URL github = new URL("https://www.marginallyclever.com/other/software-update-check.php?id=4");
	        BufferedReader in = new BufferedReader(new InputStreamReader(github.openStream()));

	        String inputLine;
	        if((inputLine = in.readLine()) != null) {
	        	if( inputLine.compareTo(version) !=0 ) {
	        		JOptionPane.showMessageDialog(null,"A new version of this software is available.  The latest version is "+inputLine+"\n"
	        											+"Please visit http://www.marginallyclever.com/ to get the new hotness.");
	        	} else {
	        		JOptionPane.showMessageDialog(null,"This version is up to date.");
	        	}
	        } else {
	        	throw new Exception();
	        }
	        in.close();
		} catch (Exception e) {
    		JOptionPane.showMessageDialog(null,"Sorry, I failed.  Please visit http://www.marginallyclever.com/ to check yourself.");
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if( subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"<html><body>"
					+"<h1>Hexapod v"+version+"</h1>"
					+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
					+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
					+"<p>To get the latest version please visit<br><a href='https://github.com/MarginallyClever/Hexapod'>https://github.com/MarginallyClever/Hexapod</a></p><br>"
					+"<p>This program is open source and free.  If this was helpful<br> to you, please buy me a thank you beer through Paypal.</p>"
					+"</body></html>");
			return;
		}
		if( subject == buttonCheckForUpdate ) {
			CheckForUpdate();
			return;
		}
		if( subject == buttonQuit ) {
			System.exit(0);
			return;
		}

		if( subject == buttonFlyUp ) {
			Vector3f up = new Vector3f(world.camera.up);
			up.scale(-1);
			world.camera.position.add(up);
		}
		if( subject == buttonFlyDown ) {
			Vector3f up = new Vector3f(world.camera.up);
			world.camera.position.add(up);
		
		}
		if( subject == buttonFlyLeft ) {
			Vector3f up = new Vector3f(world.camera.right);
			world.camera.position.add(up);		
		}
		if( subject == buttonFlyRight ) {
			Vector3f up = new Vector3f(world.camera.right);
			up.scale(-1);
			world.camera.position.add(up);		
		}
		if( subject == buttonFlyForward ) {
			Vector3f up = new Vector3f(world.camera.forward);
			up.scale(-1);
			world.camera.position.add(up);		
		}
		if( subject == buttonFlyBackward ) {
			Vector3f up = new Vector3f(world.camera.forward);
			world.camera.position.add(up);		
		}

		if( subject == buttonLookDown ) {
			world.camera.tilt-=1;
			if(world.camera.tilt < 1) world.camera.tilt = 1;
		}
		if( subject == buttonLookUp ) {
			world.camera.tilt+=1;
			if(world.camera.tilt > 179) world.camera.tilt = 179;		
		}
		if( subject == buttonLookLeft ) {
			world.camera.pan-=1;
		}
		if( subject == buttonLookRight ) {
			world.camera.pan+=1;
		}


		if( subject == buttonSpideeStand ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_STANDUP;
		}
		if( subject == buttonSpideeSit ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_SITDOWN;
		}

		if( subject == buttonSpideeUp ) {
			world.robot0.buttons[Spidee.BUTTONS_Z_POS] = ( world.robot0.buttons[Spidee.BUTTONS_Z_POS]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Z_NEG]=0;
		}
		if( subject == buttonSpideeDown ) {
			world.robot0.buttons[Spidee.BUTTONS_Z_NEG] = ( world.robot0.buttons[Spidee.BUTTONS_Z_NEG]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Z_POS]=0;
		}

		if( subject == buttonSpideeWalkStyle1 ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_RIPPLE;
		}
		if( subject == buttonSpideeWalkStyle2 ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_WAVE;		
		}
		if( subject == buttonSpideeWalkStyle3 ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_TRIPOD;
		}
		
		if( subject == buttonSpideeStill ) {
			world.robot0.move_mode=Spidee.MoveModes.MOVE_MODE_STANDUP;
			world.robot0.buttons[Spidee.BUTTONS_Z_POS]=0;
			world.robot0.buttons[Spidee.BUTTONS_Z_NEG]=0;
			world.robot0.buttons[Spidee.BUTTONS_Y_POS]=0;
			world.robot0.buttons[Spidee.BUTTONS_Y_NEG]=0;
			world.robot0.buttons[Spidee.BUTTONS_X_POS]=0;
			world.robot0.buttons[Spidee.BUTTONS_X_NEG]=0;
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_POS]=0;
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_NEG]=0;
		}

		if( subject == buttonSpideeWalkForward ) {
			world.robot0.buttons[Spidee.BUTTONS_Y_POS] = ( world.robot0.buttons[Spidee.BUTTONS_Y_POS]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Y_NEG]=0;
		}
		if( subject == buttonSpideeWalkBack ) {
			world.robot0.buttons[Spidee.BUTTONS_Y_NEG] = ( world.robot0.buttons[Spidee.BUTTONS_Y_NEG]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Y_POS]=0;
		}
		if( subject == buttonSpideeWalkLeft ) {
			world.robot0.buttons[Spidee.BUTTONS_X_POS] = ( world.robot0.buttons[Spidee.BUTTONS_X_POS]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_X_NEG]=0;
		}
		if( subject == buttonSpideeWalkRight ) {
			world.robot0.buttons[Spidee.BUTTONS_X_NEG] = ( world.robot0.buttons[Spidee.BUTTONS_X_NEG]==0 ) ? 2 : 0;
			world.robot0.buttons[Spidee.BUTTONS_X_POS]=0;
		}
		if( subject == buttonSpideeTurnLeft ) {
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_POS] = ( world.robot0.buttons[Spidee.BUTTONS_Z_ROT_POS]==0 ) ? 1 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_NEG]=0;
		}
		if( subject == buttonSpideeTurnRight ) {
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_NEG] = ( world.robot0.buttons[Spidee.BUTTONS_Z_ROT_NEG]==0 ) ? 1 : 0;
			world.robot0.buttons[Spidee.BUTTONS_Z_ROT_POS]=0;
		}
	}

	protected void LoadConfig() {
		GetRecentFiles();
	}

	protected void SaveConfig() {
		GetRecentFiles();
	}
	
	
	/**
	 * changes the order of the recent files list in the File submenu, saves the updated prefs, and refreshes the menus.
	 * @param filename the file to push to the top of the list.
	 */
	public void UpdateRecentFiles(String filename) {
		int cnt = recentFiles.length;
		String [] newFiles = new String[cnt];
		
		newFiles[0]=filename;
		
		int i,j=1;
		for(i=0;i<cnt;++i) {
			if(!filename.equals(recentFiles[i]) && recentFiles[i] != "") {
				newFiles[j++] = recentFiles[i];
				if(j == cnt ) break;
			}
		}

		recentFiles=newFiles;

		// update prefs
		for(i=0;i<cnt;++i) {
			if( recentFiles[i]==null ) recentFiles[i] = new String("");
			if( recentFiles[i].isEmpty()==false ) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		updateMenu();
	}
	
	// A file failed to load.  Remove it from recent files, refresh the menu bar.
	public void RemoveRecentFile(String filename) {
		int i;
		for(i=0;i<recentFiles.length-1;++i) {
			if(recentFiles[i]==filename) {
				break;
			}
		}
		for(;i<recentFiles.length-1;++i) {
			recentFiles[i]=recentFiles[i+1];
		}
		recentFiles[recentFiles.length-1]="";

		// update prefs
		for(i=0;i<recentFiles.length;++i) {
			if(!recentFiles[i].isEmpty()) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		updateMenu();
	}
	
	// Load recent files from prefs
	public void GetRecentFiles() {
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, recentFiles[i]);
		}
	}

	/**
	 * Open a gcode file to run on a robot.  This doesn't make sense if there's more than one robot!
	 * @param filename the file to open
	 */
	public void OpenFile(String filename) {
		
	}

    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.setSwapInterval(1);

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		//gl2.glOrtho(0, screen_width, 0, screen_height, 1, -1);
		GLU glu = new GLU();
        glu.gluPerspective(45, (float)width/(float)height, 1.0f, 1000.0f);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		
        world.setup( gl2 );
    }
    
    @Override
    public void init( GLAutoDrawable drawable ) {
    	// Use debug pipeline
    	boolean glDebug=true;
    	boolean glTrace=false;
    	
        GL gl = drawable.getGL();

        if(glDebug) {
            try {
                // Debug ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Debug", null, gl, null) );
            } catch (Exception e) {e.printStackTrace();}
        }

        if(glTrace) {
            try {
                // Trace ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Trace", null, gl, new Object[] { System.err } ) );
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    
    
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
        long now_time = System.currentTimeMillis();
        float dt = (now_time - last_time)*0.001f;
    	last_time = now_time;
    	//System.out.println(dt);
    	
		// Clear The Screen And The Depth Buffer
    	GL2 gl2 = glautodrawable.getGL().getGL2();
    	gl2.glClearColor(0,0,0,0);
    	
        // Special handling for the case where the GLJPanel is translucent
        // and wants to be composited with other Java 2D content
        if (GLProfile.isAWTAvailable() &&
            (glautodrawable instanceof javax.media.opengl.awt.GLJPanel) &&
            !((javax.media.opengl.awt.GLJPanel) glautodrawable).isOpaque() &&
            ((javax.media.opengl.awt.GLJPanel) glautodrawable).shouldPreserveColorBufferIfTranslucent()) {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        } else {
          gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        }
        
        // move everything
        world.update(dt);
        // draw the world
        world.render(gl2);

        // update input states
        Input.GetSingleton().update();        
    }
	

	@Override
	public void keyPressed(KeyEvent e) {
		Input.GetSingleton().keyPressed(e);
	}
	@Override
	public void keyReleased(KeyEvent e) {
		Input.GetSingleton().keyReleased(e);
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Input.GetSingleton().mousePressed(e);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		Input.GetSingleton().mouseReleased(e);
	}
	public void mouseDragged(MouseEvent e) {
		Input.GetSingleton().mouseDragged(e);
	}
	public void mouseMoved(MouseEvent e) {
		Input.GetSingleton().mouseMoved(e);
	}
	public void mouseClicked(MouseEvent e) {
		Input.GetSingleton().mouseClicked(e);
	}
	public void mouseEntered(MouseEvent e) {
		Input.GetSingleton().mouseEntered(e);
	}
	public void mouseExited(MouseEvent e) {
		Input.GetSingleton().mouseExited(e);
	}
	public void MouseWheelMoved(MouseWheelEvent e) {
		Input.GetSingleton().mouseWheelMoved(e);
    }
}
