package com.yeye.frontend.core

import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Style {
  // Color palette
  val darkGreen = "#1a5336"
  val lightGreen = "#2a734d"
  val purple = "#7b2cbf"
  val lightPurple = "#9d4edd"
  val white = "#ffffff"
  val lightGrey = "#f2f2f2"
  val darkGrey = "#333333"

  // Apply global styles
  def init(): Unit = {
    val styleElement = dom.document.createElement("style")
    styleElement.textContent = """
      /* Global styles */
      body {
        font-family: 'Roboto', 'Helvetica Neue', sans-serif;
        margin: 0;
        padding: 0;
        background-color: #f9f9f9;
        color: #333;
      }
      
      /* Layout */
      .app-container {
        display: flex;
        min-height: 100vh;
      }
      
      .content-container {
        flex-grow: 1;
        margin-left: 70px; /* Collapsed menu width */
        padding: 20px;
        transition: margin-left 0.3s;
      }
      
      /* Menu styling */
      .burger-menu {
        background-color: #1a5336;
        color: white;
        width: 70px; /* Collapsed state */
        height: 100vh;
        position: fixed;
        left: 0;
        top: 0;
        transition: width 0.3s ease;
        box-shadow: 3px 0 10px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        z-index: 1000;
      }
      
      .burger-menu.expanded {
        width: 250px;
      }
      
      .burger-icon {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 24px;
        width: 30px;
        cursor: pointer;
        margin: 20px auto;
      }
      
      .burger-icon .bar {
        height: 3px;
        width: 100%;
        background-color: white;
        border-radius: 3px;
      }
      
      .menu-container {
        width: 250px; /* Full menu width */
      }
      
      .menu-header {
        padding: 20px 15px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        white-space: nowrap;
      }
      
      .menu-items {
        list-style: none;
        padding: 0;
        margin: 0;
      }
      
      .menu-item {
        padding: 15px 20px;
        cursor: pointer;
        display: flex;
        align-items: center;
        transition: background-color 0.2s;
        white-space: nowrap;
      }
      
      .menu-item:hover {
        background-color: rgba(255, 255, 255, 0.1);
        border-left: 4px solid #9d4edd;
      }
      
      .menu-item.active {
        background-color: #2a734d;
        border-left: 4px solid #7b2cbf;
      }
      
      .menu-icon {
        display: inline-block;
        width: 25px;
        font-size: 18px;
        text-align: center;
      }
      
      .menu-label {
        margin-left: 10px;
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      .burger-menu.expanded .menu-label {
        opacity: 1;
      }
      
      @media (max-width: 768px) {
        .burger-menu {
          width: 0;
        }
        
        .burger-menu.expanded {
          width: 250px;
        }
        
        .content-container {
          margin-left: 0;
        }
      }
      
      /* Table styling */
      table {
        width: 100%;
        border-collapse: collapse;
        margin: 20px 0;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }
      
      th {
        background-color: #1a5336;
        color: white;
        padding: 12px 15px;
        text-align: left;
      }
      
      tr:nth-child(even) {
        background-color: #f2f2f2;
      }
      
      tr:hover {
        background-color: #e6e6e6;
      }
      
      td {
        padding: 10px 15px;
        border-bottom: 1px solid #ddd;
      }
      
      /* Landing page styling */
      .landing-page {
        max-width: 1200px;
        margin: 0 auto;
        padding: 20px;
      }
      
      .hero-section {
        text-align: center;
        padding: 60px 20px;
        margin-bottom: 40px;
      }
      
      .hero-section h1 {
        font-size: 48px;
        color: #1a5336;
        margin-bottom: 20px;
      }
      
      .hero-section p {
        font-size: 20px;
        color: #666;
        max-width: 600px;
        margin: 0 auto;
      }
      
      .features-section {
        margin-bottom: 60px;
      }
      
      .features-section h2 {
        text-align: center;
        font-size: 36px;
        color: #1a5336;
        margin-bottom: 40px;
      }
      
      .feature-cards {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 20px;
      }
      
      .feature-card {
        flex: 1;
        min-width: 300px;
        background-color: white;
        border-radius: 8px;
        padding: 30px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        transition: transform 0.3s, box-shadow 0.3s;
      }
      
      .feature-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
      }
      
      .feature-icon {
        font-size: 36px;
        margin-bottom: 20px;
      }
      
      .feature-card h3 {
        font-size: 24px;
        color: #1a5336;
        margin-bottom: 15px;
      }
      
      .cta-section {
        text-align: center;
        padding: 60px 20px;
        background-color: #f2f2f2;
        border-radius: 8px;
      }
      
      .cta-section h2 {
        font-size: 36px;
        color: #1a5336;
        margin-bottom: 20px;
      }
      
      .cta-section button {
        padding: 12px 30px;
        font-size: 18px;
      }
      
      /* Button styling */
      button {
        padding: 8px 16px;
        background-color: #1a5336;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.2s;
      }
      
      button:hover {
        background-color: #2a734d;
      }
      
      button.secondary {
        background-color: #7b2cbf;
      }
      
      button.secondary:hover {
        background-color: #9d4edd;
      }
      
      /* Form elements */
      input, select, textarea {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        width: 100%;
        margin-bottom: 15px;
      }
      
      input:focus, select:focus, textarea:focus {
        outline: none;
        border-color: #7b2cbf;
        box-shadow: 0 0 0 2px rgba(123, 44, 191, 0.2);
      }
      
      label {
        display: block;
        margin-bottom: 5px;
        font-weight: 500;
      }
      
      /* Error and loading states */
      .error-message {
        background-color: #ffe6e6;
        color: #d32f2f;
        padding: 12px 15px;
        border-radius: 4px;
        margin-bottom: 20px;
        border-left: 4px solid #d32f2f;
      }
      
      .loading-spinner {
        padding: 15px;
        text-align: center;
        color: #1a5336;
      }
      
      /* Form layouts */
      .filter-section, .add-user-form {
        background-color: white;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 20px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }
      
      .form-row {
        display: flex;
        gap: 15px;
        align-items: flex-end;
      }
      
      .form-row input {
        margin-bottom: 0;
      }
      
      .table-container {
        background-color: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }
    """

    dom.document.head.appendChild(styleElement)
  }
}
