/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileoperations;

import core.PhotoFunctions;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author 633630
 */
@WebServlet(name = "DeletePhoto", urlPatterns = {"/DeletePhoto"})
public class DeletePhoto extends HttpServlet {
@EJB
    private PhotoFunctions pf;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * 
     * This servlet handles the deletion of photos. It is passed a photo ID from the photoUpgraded.jsp page, using it
     * to delete a photo from both the database and the physical hard drive. It also uses the photoId to check whether
     * or not the user who uploaded it is the same as the user trying to delete it, so as to ensure security.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        
        
        String photoDelete = request.getParameter("photoId");
        int photoId = 0;
        if(photoDelete!=null)
        {
            photoId = Integer.parseInt(photoDelete);
        }
        String artist = pf.getArtist(photoId);
        HttpSession session = request.getSession();
        String username = (String)session.getAttribute("username");
        if(username.equals(artist) && photoDelete!=null)
        {
            String location = pf.getLocation(photoId);
            location = location.replace("/Capstone-war/", "C:\\var\\webapp\\");
            Path pathLocation = Paths.get(location);
            Files.delete(pathLocation);
            pf.photoDelete(photoId);
            response.sendRedirect("fileupload.jsp?artist=" + (String)session.getAttribute("username"));
        }
        else
            response.sendRedirect("fileupload.jsp?artist=" + (String)session.getAttribute("username") + "&message=Cannot delete photo");
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
