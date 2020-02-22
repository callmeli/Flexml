package com.guet.flexbox.build

import com.guet.flexbox.transaction.PageContext
import com.guet.flexbox.TemplateNode
import com.guet.flexbox.build.attrsinfo.AttributeInfo
import com.guet.flexbox.build.attrsinfo.AttrsInfoRegistry
import com.guet.flexbox.el.ELContext

internal inline val CharSequence.isExpr: Boolean
    get() = length > 3 && startsWith("\${") && endsWith('}')

internal typealias AttributeInfoSet = Map<String, AttributeInfo<*>>

internal inline fun create(crossinline action: AttrsInfoRegistry.() -> Unit): Lazy<AttributeInfoSet> {
    return lazy {
        AttrsInfoRegistry().apply(action).value
    }
}

typealias AttributeSet = Map<String, Any>

internal typealias Converter<T> = (PageContext, ELContext, String) -> T?

typealias RenderNodeFactory = (
        visibility: Boolean,
        attrs: AttributeSet,
        children: List<Child>,
        other: Any
) -> Any

typealias Child = Any

typealias ToWidget = Pair<Declaration, RenderNodeFactory?>

internal fun ToWidget.toWidget(
        bindings: BuildTool,
        template: TemplateNode,
        pageContext: PageContext,
        data: ELContext,
        upperVisibility: Boolean,
        other: Any
): List<Child> {
    return first.transform(
            bindings,
            template.attrs ?: emptyMap(),
            template.children ?: emptyList(),
            second,
            pageContext,
            data,
            upperVisibility,
            other
    )
}

