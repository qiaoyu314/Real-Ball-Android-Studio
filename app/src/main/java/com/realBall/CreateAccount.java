package com.realBall;


import java.util.List;

import com.realBall.R;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity for creating a new account
 * @author Yu, Marty and Lingchen
 *
 */
public class CreateAccount extends Activity implements OnClickListener{
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfirm;
	private DatabaseHelper dh;
	private final String DEFAULTLEVEL = "1";
	   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        //
        etUsername= (EditText)findViewById(R.id.username);
        etPassword= (EditText)findViewById(R.id.password);
        etConfirm = (EditText)findViewById(R.id.password_confirm);
        View btnAdd= (Button)findViewById(R.id.done_button);
        btnAdd.setOnClickListener(this); 
        View btnCancel= (Button)findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(this);
    }

    /**
     * Used for creating a new account
     */
    private void CreateAccount(){
    	String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String confirm	= etConfirm.getText().toString();
        // check whether the username and password is valid
        this.dh = new DatabaseHelper(this);
    	this.dh.open();
        List<String> names=this.dh.selectName(username);
        if ((password.equals(confirm))&&(!username.equals(""))&&(!password.equals(""))&&(!confirm.equals(""))&&names.size()==0)
        {
        	this.dh.insert(username, password);
        	this.dh.close();
        	Toast.makeText(CreateAccount.this, "new record inserted",Toast.LENGTH_SHORT).show();
        	finish();
        }
        // if username or password is invalid then remind user to do it again
        else if((username.equals(""))||(password.equals(""))||(confirm.equals("")))
        {
        	new AlertDialog.Builder(this)
    		.setTitle("Error")
    		.setMessage("Miss entry.")
    		.setNeutralButton(" Try Again", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {}
    		})
    		.show();
        }
        // if confirm password is not equal to the password then pop up a dialog to alert the user 
		else if(names.size()>0)
		{
			new AlertDialog.Builder(this)
    		.setTitle("Error")
    		.setMessage("Username exits.")
    		.setNeutralButton(" Try Again", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {}
    		})
    		.show();
		}
        else if(!password.equals(confirm))
        {
           	new AlertDialog.Builder(this)
    		.setTitle("Error")
    		.setMessage("passwords do not match")
    		.setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {}
    		})
    		
    		.show();
        }
    }
    //set on click listener
    public void onClick(View v) {
		switch (v.getId()) {
  		case R.id.done_button:
		    CreateAccount();
		    break;
		case R.id.cancel_button:
		   	finish();
	    	break;
		}
    }
}

