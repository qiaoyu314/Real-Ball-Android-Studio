package com.realBall;

import java.util.List;

import com.realBall.R;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity implements OnClickListener{
	   private DatabaseHelper dh;
	   private EditText userNameEditableField;
	   private EditText passwordEditableField;
	   private final static String OPT_NAME="name";
	   public static int nowLevel;
	   public static String nowUserName;
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.login);
	        
	        userNameEditableField=(EditText)findViewById(R.id.username_text);
	        passwordEditableField=(EditText)findViewById(R.id.password_text);
	        View btnLogin=(Button)findViewById(R.id.login_button);
	        btnLogin.setOnClickListener(this);
	        View btnCancel=(Button)findViewById(R.id.cancel_button);
	        btnCancel.setOnClickListener(this);
	        View btnNewUser=(Button)findViewById(R.id.new_user_button);
	        btnNewUser.setOnClickListener(this);
	     }
	    
	    // Check the username and password when the user login
	    private void checkLogin(){
	    	String username=this.userNameEditableField.getText().toString();
	        String password=this.passwordEditableField.getText().toString();
	        this.dh=new DatabaseHelper(this);
	        this.dh.open();
	        List<String> names=this.dh.selectAll(username,password);
	        if(names.size() >0){ // if login successful
	        	// save the username and level
	        	nowUserName = username;
	        	nowLevel = Integer.parseInt(names.get(2));
	        	SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(this);
	        	SharedPreferences.Editor editor=settings.edit();
	            editor.putString(OPT_NAME, username);
	            editor.commit();
	            this.dh.close();
	        	// Bring up the GameOptions screen
	        	startActivity(new Intent(this, MenuList.class));
	        	finish();
	        } else {
	            // if fail then pop up a dialog and the user can try again to login 
	        	new AlertDialog.Builder(this)
	    		.setTitle("Error")
	    		.setMessage("Login failed")
	    		.setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int which) {}
	    		})
	    		.show();
	        }
	      }
	    
	    public void onClick(View v) {
			switch (v.getId()) {
	  		case R.id.login_button:
			    checkLogin();
			    break;
	  		case R.id.cancel_button:
		    	finish();
	    		break;
	    	case R.id.new_user_button:
	    	    startActivity(new Intent(this, CreateAccount.class));
	    	    break;
			}
	    }
	}
