import java.beans.Statement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


@MultipartConfig
@WebServlet(name = "responseServlet", urlPatterns = {"/image"})
public class responseServlet extends HttpServlet {
    String ImagePath = "C:\\Users\\pc\\NetBeansProjects\\Networks2_HTTP_ASSIGNMENT\\hello\\src\\java\\Images\\"; 
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    final int dataChunkSize = 1;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (OutputStream os = response.getOutputStream()) {  
		Part file = request.getPart("image");
		String imageName = file.getSubmittedFileName();  // get selected image file name
		System.out.println("Uploaded Image Name : " + imageName);  
                Connection conn = null;
		Statement stmt = null;
		try {
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (Exception e) {
				System.out.println(e);
			}
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/images", "root", "");
			System.out.println("Connection is created successfully:");
                        
                        
                        String sql = ("SELECT * FROM images ");
                        PreparedStatement st1 = conn.prepareStatement("SELECT * FROM images ");
                        ResultSet rs = st1.executeQuery(sql);
                        
                        int j=0;
                        while (rs.next()) {
                            if(!rs.getString("name").equals(imageName)) j++ ;
                            else { response.setIntHeader("Duplicated",1); response.setIntHeader("Maximum",0); return; }
                        } 
                        
                        if(j >=12 ){
                            response.setIntHeader("Duplicated",0);
                            response.setIntHeader("Maximum",1);
                            return;
                        }
                        response.setIntHeader("Duplicated",0);
                        response.setIntHeader("Maximum",0);
                        PreparedStatement st = conn.prepareStatement("insert into images values(?, ? ,?)");
                        st.setInt(1, j);
                        st.setString(2, imageName);
                        st.setString(3, request.getParameter("imageDescription") );
                        st.executeUpdate();
                        st.close();
                        conn.close();
                        
                        System.out.println("Image is inserted in the table successfully");
                        
                
		} catch (SQLException excep) {
			excep.printStackTrace();
		} catch (Exception excep) {
			excep.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}        
		String uploadPath = "C:\\Users\\pc\\NetBeansProjects\\Networks2_HTTP_ASSIGNMENT\\hello\\src\\java\\Images\\"+imageName; 
		System.out.println("Server Path : " + uploadPath);
                int i = 1;
                //Wrtite the Data of the image
                byte[] data = null;
                try
		{
                    File tmp = new File(uploadPath);
                    if(tmp.exists()) uploadPath = "C:\\Users\\pc\\NetBeansProjects\\Networks2_HTTP_ASSIGNMENT\\hello\\src\\java\\Images\\"+"("+ i +")"+imageName; 
                    FileOutputStream fileWrite = new FileOutputStream(uploadPath);
                    i++;
                    InputStream is = file.getInputStream();
                    data = new byte[ is.available() ];
                    is.read(data);
                    fileWrite.write(data);
                    fileWrite.close();
                 }
                    catch(IOException e)
                    {
			System.out.println(e.toString());
                    }
                
        
            }
        }
    

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        	Connection conn = null;
		Statement stmt = null;
        	try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception e) {
                    System.out.println(e);
		}
        try {
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/images", "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(responseServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
		System.out.println("Connection is created successfully:");
                       
        
        if( request.getParameter("param").equals("getImages") && request.getParameter("fileName").isEmpty() ){
            //READ from dataBase (names) + Des

		try {

                        String sql = ("SELECT * FROM images ");
                        PreparedStatement st = conn.prepareStatement("SELECT * FROM images");
                        ResultSet rs = st.executeQuery(sql);
                        OutputStream out = response.getOutputStream();
                        while (rs.next()) {
                                String name = rs.getString("name");
                                String description = rs.getString("Description");
                                out.write((name+'\r').getBytes());  
                        } 
		} catch (SQLException excep) {
			excep.printStackTrace();
		} catch (Exception excep) {
			excep.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
            

        }
        //HERE WE CHECK IF THE WANTED GET REQUEST IS ABOUT A SPECIFIC WANTED FILE FROM THE SERVER
        else if( request.getParameter("param").equals("specificFile") &&  !request.getParameter("fileName").isEmpty()){
            String Des = getDescFromDataBase(request.getParameter("fileName"));
            transmittingFileToClient( request.getParameter("fileName") , Des , response ); 
        }
        
        else if( request.getParameter("param").equals("Mod") &&  !request.getParameter("fileName").isEmpty() &&  !request.getParameter("des").isEmpty()){           
           //MODIFY
           PrintWriter out1 = response.getWriter(); 
           java.sql.Statement stmt1 = null;
                    try {
                        stmt1 = conn.createStatement();
                    } catch (SQLException ex) {
                        Logger.getLogger(responseServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
           out1.print("<h2>INSIDE UPDATE</h2>");
           String update = "Update images set Description ='"+request.getParameter("des")+"' where name='"+request.getParameter("fileName")+"'"; 
                    try { 
                        stmt1.executeUpdate(update);
                    } catch (SQLException ex) {
                        Logger.getLogger(responseServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
         }
        
        else if( request.getParameter("param").equals("Delete") &&  !request.getParameter("fileName").isEmpty()){           
            PrintWriter out1 = response.getWriter(); 
            java.sql.Statement stmt1 = null;
                    try {
                        stmt1 = conn.createStatement();
                    } catch (SQLException ex) {
                        Logger.getLogger(responseServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 out1.print("<h2>INSIDE DELL</h2>");
                 File fqqile;
                 fqqile = new File(ImagePath + request.getParameter("fileName"));
                 fqqile.delete(); 
                 String deletestatement = "delete from images where name='"+request.getParameter("fileName")+"'";
                    try {
                        stmt1.executeUpdate(deletestatement);
                    } catch (SQLException ex) {
                        Logger.getLogger(responseServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 
        }
        
        
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                processRequest(request, response);
    }


    private static byte[] readFileToByteArray(File file) {
        FileInputStream fis = null;
        byte[] bArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();
        } catch (IOException ioExp) { System.out.println(ioExp.toString()); }
        return bArray;
    }

    private void transmittingFileToClient(String fileName , String imageDescription , HttpServletResponse response ) throws IOException {
        ServletOutputStream output = response.getOutputStream(); 
        ServletContext sc = getServletContext();
        byte[] alldata = null;  
        byte[] dataChunk = new byte[ dataChunkSize ]; 
        InputStream is =null;
        boolean LastChunk;
        File imageFile = null;
        imageFile = new File("C:\\Users\\pc\\NetBeansProjects\\Networks2_HTTP_ASSIGNMENT\\hello\\src\\java\\Images\\"+fileName);
        //is = this.getClass().getResourceAsStream("Images/"+fileName);
        if (!imageFile.exists()) {
            response.setContentType("text/plain");
            System.out.println("responseServlet.transmittingFileToClient()");
            output.write("Failed to send image".getBytes());
            
        } 
        //IF THE FILE EXISTS => WE NEED TO TRANSMIT IT
        else {
                alldata = readFileToByteArray(imageFile);
                Integer dataSize = alldata.length;
                //We put in the header the size of the file we want to send (TO THE CLIENT)
                response.setIntHeader("Size", dataSize);
                
                response.setHeader("Description", imageDescription);
                //if the data is smaller than 1KB, we send it in one transmission
                if(dataSize<=dataChunkSize){
                    output.write(alldata , 0 , dataSize);
                }

                //else if the data is bigger, then we need multi transmissions
                else{
                    for (int i = 0; i < dataSize ; i+=dataChunkSize) {
                        
                        if ((i + dataChunkSize) >= dataSize) { // LAST PACKET
                            System.arraycopy(alldata, i, dataChunk , 0 , dataSize - i);
                            output.write(dataChunk , 0 , dataSize - i);
                        } else {
                             System.arraycopy(alldata , i , dataChunk , 0 , dataChunkSize);  
                             output.write(dataChunk , 0 , dataChunkSize);
                        } 
                    }  
                }
        }     
    }
    
    
    public String getDescFromDataBase(String nameToFindDes){
        	Connection conn = null;
		Statement stmt = null;
		try {
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (Exception e) {
				System.out.println(e);
			}
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/images", "root", "");
			System.out.println("Connection is created successfully:");
                       
                        String sql = ("SELECT * FROM images ");
                        PreparedStatement st = conn.prepareStatement("SELECT * FROM images");
                        ResultSet rs = st.executeQuery(sql);
                        while (rs.next()) {
                                String name = rs.getString("name");
                                String description = rs.getString("Description");
                                if( name.equals(nameToFindDes) ) return description;
                        } 
                        
		} catch (SQLException excep) {
			excep.printStackTrace();
		} catch (Exception excep) {
			excep.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
        return null;
    
    
    }
    
    
    

}
