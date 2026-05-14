/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.salon.servlet;

import com.salon.utils.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/commission")
public class CommissionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // SQL: Kết hợp bảng STAFFS và INVOICE_DETAILS, dùng hàm SUM để tính tổng hoa hồng
        // NVL(..., 0) giúp những thợ chưa có khách sẽ hiển thị là 0đ thay vì bị lỗi null
        String sql = "SELECT s.staff_code, s.full_name, s.role, "
                   + "NVL(SUM(id.commission_value), 0) AS total_commission "
                   + "FROM STAFFS s "
                   + "LEFT JOIN INVOICE_DETAILS id ON s.staff_id = id.staff_id "
                   + "WHERE s.is_active = 1 AND s.role IN ('Stylist', 'Assistant') "
                   + "GROUP BY s.staff_code, s.full_name, s.role "
                   + "ORDER BY total_commission DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                
                out.print("{");
                out.print("\"code\":\"" + rs.getString("staff_code") + "\",");
                out.print("\"name\":\"" + rs.getString("full_name") + "\",");
                out.print("\"role\":\"" + rs.getString("role") + "\",");
                out.print("\"commission\":" + rs.getDouble("total_commission"));
                out.print("}");
                
                first = false;
            }
            out.print("]");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }
}