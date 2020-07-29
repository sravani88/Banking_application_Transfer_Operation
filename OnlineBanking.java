import java.sql.Statement;

import java.util.ArrayList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.CallableStatement;

/*
 * Browser sends Http Request to Web Server
 * 
 * Code in Web Server => Input:HttpRequest, Output: HttpResponse
 * JEE with Servlets
 * 
 * Web Server responds with Http Response
 */

//Java Platform, Enterprise Edition (Java EE) JEE6

//Servlet is a Java programming language class 
//used to extend the capabilities of servers 
//that host applications accessed by means of 
//a request-response programming model.

//1. extends javax.servlet.http.HttpServlet
//2. @WebServlet(urlPatterns = "/login.do")
//3. doGet(HttpServletRequest request, HttpServletResponse response)
//4. How is the response create
@WebServlet(urlPatterns = "/OnlineBanking")
public class OnlineBanking extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
		//forwarding request and response to firstpage.jsp to perform transcation operation
		request.getRequestDispatcher("firstpage.jsp").forward(request,response);

	}
	public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException,ServletException{
		//arraylist to store last five transcation 
		ArrayList<Integer> trans_list=new ArrayList<Integer>();
		//getting amount that the user had entered to transfer money
		String amount=request.getParameter("amount");
		Connection mycon=null;
		Statement mystmt=null,mystmt1=null;
		ResultSet myrs=null,myrs1=null,myrs2=null;
		CallableStatement callmystmt=null;
		//checking whether user entered amount or not if the amount is empty asking using to enter money to transfer
		if(amount=="")
		{
			request.getSession().setAttribute("errmsg", "amount should not be empty");
			request.getRequestDispatcher("firstpage.jsp").forward(request, response);
			return;
		}//end of if
		else
		{
		try {
			//creating connection to database
			mycon=DriverManager.getConnection("jdbc:mysql://localhost:3306/demo","root","Sravs");
			mystmt=mycon.createStatement();
			mystmt1=mycon.createStatement();
			//selecting balance and accountid from bankdetails table from database
			myrs=mystmt.executeQuery("select balance,acc_id from bankdetails");
			while(myrs.next()) {
				int updatebalance=myrs.getInt("balance");
				int accountid=myrs.getInt("acc_id");
				//checking whether user entered valid amount or not or checking whether user entered sufficient balance
				if(Integer.parseInt(amount)<=updatebalance) {
					//if it is correct updating the balance in the database after clicking on transfer button
				updatebalance=updatebalance-Integer.parseInt(amount);
				PreparedStatement preparestmt=mycon.prepareStatement("update bankdetails set balance=? where acc_id=?");
				preparestmt.setInt(1,updatebalance);
				preparestmt.setInt(2, accountid);
				preparestmt.executeUpdate();
				//select count of rows in translist table
				myrs2=mystmt1.executeQuery("select count(*) as countlist from translist");
				//inserting transcation details into another table(translist)
				while(myrs2.next()) {
				int count=myrs2.getInt("countlist");
				PreparedStatement preparestmt1=mycon.prepareStatement("insert into translist(serialno,acc_id,amount)values(?,?,?)");
				preparestmt1.setInt(2,accountid);
				preparestmt1.setInt(3, Integer.parseInt(amount));
				preparestmt1.setInt(1, count+1);
				preparestmt1.executeUpdate();
				}//end of while
				//creating callable statement to get last five elements that was created in database
				callmystmt=mycon.prepareCall("{call get_transcation_list(?)}");
				callmystmt.setInt(1,accountid);
				callmystmt.execute();
				myrs1=callmystmt.getResultSet();
				//after calling the procedure displaying the result
				while(myrs1.next()) {
					//storing last five transaction into the list
					int  trans_amount=myrs1.getInt("amount");
					trans_list.add(trans_amount);
					
				}//end of while
				//forwarding details into the onlinebanking.jsp five to display balance and last 5 transaction
					request.setAttribute("transferlist", trans_list);
					request.setAttribute("Availablebalance", updatebalance);
					request.getRequestDispatcher("OnlineBanking.jsp").forward(request, response);
					

				
				}//end of if
				else {
					//if there is insufficient balance displaying errormessage
					request.getSession().setAttribute("errormsg", "You don't have sufficient balance to transfer");
					request.getRequestDispatcher("firstpage.jsp").forward(request, response);
				}//end of else
			}//end of while
			
		}//end of try
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(mycon!=null)
				try {
					mycon.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if(mystmt!=null)
				try {
					mystmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if(myrs!=null)
				try {
					myrs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		}
	}


}
