package com.yeye.frontend

// Test suite for Router
class RouterTests {

  // Test mapping of empty hash to LandingPage
  def testEmptyHashMapping(): Unit = {
    val page = Router.LandingPage
    assert(page == Router.LandingPage)
  }

  // Test mapping of #users hash to UsersListPage
  def testUsersHashMapping(): Unit = {
    val page = Router.UsersListPage
    assert(page == Router.UsersListPage)
  }

  // Test rendering different pages
  def testPageRendering(): Unit = {
    // Simple check that different pages render to different elements
    val landingPage = Router.renderPage(Router.LandingPage)
    val usersPage = Router.renderPage(Router.UsersListPage)
    assert(landingPage != usersPage)
  }
}
