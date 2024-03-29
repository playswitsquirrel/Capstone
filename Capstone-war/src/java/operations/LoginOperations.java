/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operations;
import core.MemberFunctions;
import domains.Member;
import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
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
@WebServlet(name = "LoginOperations", urlPatterns = {"/LoginOperations"})
public class LoginOperations extends HttpServlet {
    @EJB
    private MemberFunctions mf;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * This servlet is used to login the user, verifying the details entered
     * in the process.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String logout = request.getParameter("logout");
        
        /*
         * check if user has logged out; invalidate his session
         */
        if(logout!=null)
        {
            HttpSession session = request.getSession();
            session.invalidate();
            response.sendRedirect("index.jsp?message=Successfully Logged Out");
        }
        /*
         *  Check if username and password fields were not submitted blank
         *  Check the database to see if the user and password combination exists
         *  Check whether the user has been locked
         *  If those checks pass, set session variables for username, id, and type
         *  Redirect depending on user's type; if a, go to admin page, if m or u,
         *  go to index
         *  If checks don't pass, redirect back to index page with a relevant 
         *  message
         */
            else if(username!=null && !username.equals("") && password!=null && !password.equals(""))
            {
                if(mf.memberValidate(username, password))
                {
                    if(!mf.memberCheckLocked(username))
                    {
                        Member member = mf.getMemberInfo(username);
                        HttpSession session = request.getSession();
                        session.setAttribute("username", member.getEmail());
                        session.setAttribute("userId", member.getIdMember());
                        session.setAttribute("firstName", member.getFirstName());
                        session.setAttribute("lastName", member.getLastName());
                        session.setAttribute("type", (""+member.getAccountType()));
                        
                        if(member.getAccountType() == 'a')
                            response.sendRedirect("admin.jsp");
                        
                        else if(member.getAccountType() == 'm')
                            response.sendRedirect("index.jsp?message=Logged in");
                        
                        else if(member.getAccountType() == 'u')
                            response.sendRedirect("fileupload.jsp?artist="+member.getEmail());
                    }
                    else
                        response.sendRedirect("index.jsp?message=User Account is Locked");
                }
                else
                    response.sendRedirect("index.jsp?message=Incorrect username or password");
            }
            else
                response.sendRedirect("index.jsp?message=All values are required");
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
