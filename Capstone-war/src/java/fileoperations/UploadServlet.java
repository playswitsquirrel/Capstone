/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileoperations;

import brokers.PhotoBroker;
import core.PhotoFunctions;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 *
 * @author 633630
 */
@WebServlet(name = "UploadServlet", urlPatterns = {"/UploadServlet"})
@MultipartConfig
public class UploadServlet extends HttpServlet {
    @EJB
    private PhotoFunctions pf;
    private PhotoBroker pb;
    private final static Logger LOGGER = Logger.getLogger(UploadServlet.class.getCanonicalName());
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * This servlet manages the upload of a picture, making sure it is indeed a picture as well as sanitizing the 
     * user's inputs. It uploads the photo a location on the harddrive: c:var\\webapp\\uploads, as well as uploads
     * the photo's details and tags to the database.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        int userId = (Integer)session.getAttribute("userId");
        //folder to place the uploaded file
        final String path = "C:\\var\\webapp\\uploads\\";
        final Part filePart = request.getPart("file");
        final String fileName = getFileName(filePart);
        long currentTimeMillis= System.currentTimeMillis();
        String title = request.getParameter("title");
        String tags = request.getParameter("tags");
        String description = request.getParameter("description");
        int price = -1;
        int exclusivePrice = -1;
        description = sanitize(description);
        title = sanitize(title);
        tags = sanitize(tags);

        String priceString = request.getParameter("price");
        String ExclusivePriceString = request.getParameter("exclusivePrice");
        if(priceString != null && ExclusivePriceString != null)
        {
            price = Integer.parseInt(request.getParameter("price"));
            exclusivePrice = Integer.parseInt(request.getParameter("exclusivePrice"));
        }

        String mimeType;
        
        OutputStream out = null;
        InputStream filecontent = null;
        /*
            This bit checks the file's data stream and stores the file's type
            in the mimeType variable
        */
        try (InputStream input = new BufferedInputStream(filePart.getInputStream()))
        {
            mimeType = URLConnection.guessContentTypeFromStream(input);
            input.close();
        }
        // checks if file is an image, makes sure fields are filled in
        if(description!=null && !description.equals("") && title!=null && !title.equals("") && tags!=null && !tags.equals("") && mimeType!=null && mimeType.startsWith("image") && description.length() <= 800 && price > 0 && exclusivePrice > 0)
        {
            try {
                out = new FileOutputStream(new File(path + userId + currentTimeMillis + fileName));
                filecontent = filePart.getInputStream();
                
                int read = 0;
                final byte[] bytes = new byte[1024];

                while((read = filecontent.read(bytes)) != -1)
                {
                    out.write(bytes, 0, read);
                }
                LOGGER.log(Level.INFO, "File{0}being uploaded to {1}", new Object[]{fileName, path});
            } 
            catch (FileNotFoundException fne)
            {
                System.out.println("Could not upload files");
            }
            finally
            {
                String[] tagArray = tags.toLowerCase().split(" ");

                String location = addPhoto(fileName, description, title, userId, currentTimeMillis, path, price, exclusivePrice);
                int photoId = pf.getId(location);
                pf.addTag(photoId, tagArray);
                if(out!=null)
                {
                    out.close();
                }
                if(filecontent!=null)
                {
                    filecontent.close();
                }
                response.sendRedirect("fileupload.jsp?artist=" + (String)session.getAttribute("username") + "&message=File uploaded");
            }
        }
        else
        {
            response.sendRedirect("fileupload.jsp?artist=" + (String)session.getAttribute("username") + "&message=Prices cannot be 0!");
        }     
    }
    private String getFileName(final Part part)
    {
        for(String content : part.getHeader("content-disposition").split(";"))
        {
            if(content.trim().startsWith("filename"))
            {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    
    /*
        This function is used to create a location string from passed in values,
        as well as to call the photoAdd function from the photo broker. 
        It returns the location string for use in finding the photo's id, so that
        tags can be added
    */
    private String addPhoto(String fileName, String description, String title, int userId, long currentTimeMillis, String path, int price, int exclusivePrice)
    {
        String location = "/Capstone-war/uploads/" + userId + currentTimeMillis + fileName;
        pf.photoAdd(userId, 1, title, description, location, price, exclusivePrice);
        return location;
    }
    
    private String sanitize(String dirtyInput)
    {
        String cleanInput = "";
        try {
            final String POLICY_LOCATION = getServletContext().getRealPath("WEB-INF/policy.xml");
            Policy policy = Policy.getInstance(POLICY_LOCATION);
            AntiSamy as = new AntiSamy();
            CleanResults cr = as.scan(dirtyInput, policy, AntiSamy.DOM);
            cleanInput = cr.getCleanHTML();
        } catch (PolicyException | ScanException ex) {
            Logger.getLogger(UploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cleanInput;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
