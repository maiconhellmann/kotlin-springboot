@file:Suppress("DEPRECATION")

package com.github.maiconhellmann.demo.config.misc

import org.springframework.security.crypto.password.MessageDigestPasswordEncoder

class ShaPasswordEncoder @JvmOverloads constructor(strength: Int = 256) : MessageDigestPasswordEncoder("SHA-$strength")