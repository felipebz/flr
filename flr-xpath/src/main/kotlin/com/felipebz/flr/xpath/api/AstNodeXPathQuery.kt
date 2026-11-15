//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.xpath.api

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.impl.xpath.AstNodeNavigator
import org.jaxen.BaseXPath
import org.jaxen.JaxenException

public class AstNodeXPathQuery<T> private constructor(xpath: String) {
    private val astNodeNavigator = AstNodeNavigator()
    private var expression: BaseXPath = try {
        BaseXPath(xpath, astNodeNavigator)
    } catch (e: JaxenException) {
        throw RuntimeException(e)
    }

    /**
     * Evaluate the XPath query on the given AstNode and returns the first result, or null if there was no result.
     *
     * <pre>
     * In the following case, AstNodeXpathQuery.create('/a/b').selectSingleNode(node) would return the AstNode of B2.
     *
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @param astNode
     * The AstNode on which to evaluate the query against to.
     * @return The first result or null if there was no result.
     */
    @Suppress("UNCHECKED_CAST")
    public fun selectSingleNode(astNode: AstNode): T {
        return try {
            astNodeNavigator.reset()
            expression.selectSingleNode(astNode) as T
        } catch (e: JaxenException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Evaluate the XPath query on the given AstNode and returns all matching elements.
     *
     * <pre>
     * In the following case, AstNodeXpathQuery.create('/a/b').selectNodes(node) would return the AstNode of B2 and B3, in that order.
     *
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @param astNode
     * The AstNode on which to evaluate the query against to.
     * @return The list of resulting elements, empty when no result available.
     */
    @Suppress("UNCHECKED_CAST")
    public fun selectNodes(astNode: AstNode): List<T> {
        return try {
            astNodeNavigator.reset()
            expression.selectNodes(astNode) as List<T>
        } catch (e: JaxenException) {
            throw RuntimeException(e)
        }
    }

    public companion object {
        /**
         * Creates a compiled XPath query, which can be evaluated multiple times on different AstNode.
         *
         * @param xpath
         * The query to compile
         * @return The compiled XPath query
         */
        @JvmStatic
        public fun <E> create(xpath: String): AstNodeXPathQuery<E> {
            return AstNodeXPathQuery(xpath)
        }
    }

}
