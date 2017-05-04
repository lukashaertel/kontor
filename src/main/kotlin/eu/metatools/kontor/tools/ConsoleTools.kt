package eu.metatools.kontor.tools

/**
 * Returns all user input lines until empty or the input stream has ended.
 */
val consoleLines = generateSequence(::readLine).takeWhile(String::isNotEmpty)