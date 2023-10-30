package com.nf.mvc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpRequestHandler {
    void processRequest(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException;
}
