package com.yeye.frontend

/** Helper object for tests
  *
  * This object provides utilities and common functionality for tests. It serves
  * as a central place for test configuration and shared test behavior.
  */
object TestHelper {

  /** Flag indicating whether we're in test mode */
  val testMode = true

  /** Creates a mock DOM element for testing
    *
    * In a real implementation, this would create a mock element, but since we
    * don't have the necessary testing libraries in this example, it just
    * returns a placeholder.
    *
    * @return
    *   A string representation of a mock element
    */
  def createMockElement(): String = "<div>Mock Element</div>"

  /** Helper method to assert equality in tests
    *
    * @param actual
    *   The actual value
    * @param expected
    *   The expected value
    * @return
    *   true if the values are equal, false otherwise
    */
  def assertEqual[T](actual: T, expected: T): Boolean = actual == expected
}
