(function() {
  const CONTAINER_MINI_WIDTH = 4;
  const CONTAINER_MINI_HEIGHT = 4;
  const nodeSizeThreshold = 4;
  
  const NodeType = {
    BUTTON: 'button',
    IMG: 'img',
    TEXT: 'text',
    FORM_ITEM: 'form_item',
    A: 'a',
    CONTAINER: 'container'
  };

  let idCounter = 1;

  function isFormElement(node) {
    if (!(node instanceof HTMLElement)) return false;
    const tag = node.tagName.toLowerCase();
    return ['input', 'textarea', 'select', 'option'].includes(tag);
  }

  function isButtonElement(node) {
    if (!(node instanceof HTMLElement)) return false;
    const tag = node.tagName.toLowerCase();
    const role = node.getAttribute('role');
    return tag === 'button' || role === 'button';
  }

  function isAElement(node) {
    if (!(node instanceof HTMLElement)) return false;
    return node.tagName.toLowerCase() === 'a';
  }

  function isSvgElement(node) {
    return node instanceof SVGElement;
  }

  function isImgElement(node) {
    if (node instanceof Element) {
      try {
        const computedStyle = window.getComputedStyle(node);
        const backgroundImage = computedStyle.getPropertyValue('background-image');
        if (backgroundImage && backgroundImage !== 'none') {
          return true;
        }
      } catch (e) {}
    }

    if (isIconfont(node)) {
      return true;
    }

    if (node instanceof HTMLElement && node.tagName.toLowerCase() === 'img') {
      return true;
    }
    if (node instanceof SVGElement && node.tagName.toLowerCase() === 'svg') {
      return true;
    }
    return false;
  }

  function isIconfont(node) {
    if (node instanceof Element) {
      try {
        const computedStyle = window.getComputedStyle(node);
        const fontFamilyValue = computedStyle.fontFamily || '';
        return fontFamilyValue.toLowerCase().indexOf('iconfont') >= 0;
      } catch (e) {}
    }
    return false;
  }

  function isTextElement(node) {
    if (node instanceof Element) {
      if (node.childNodes && node.childNodes.length === 1 && node.childNodes[0].nodeType === Node.TEXT_NODE) {
        return true;
      }
    }
    return node.nodeType === Node.TEXT_NODE && !isIconfont(node);
  }

  function isContainerElement(node) {
    if (!(node instanceof HTMLElement)) return false;

    // Check if it includes text or child interactive tags
    if (node.innerText && node.innerText.trim()) {
      return false;
    }

    const includeList = ['svg', 'button', 'input', 'textarea', 'select', 'option', 'img', 'a'];
    for (let i = 0; i < includeList.length; i++) {
      if (node.querySelector(includeList[i])) {
        return false;
      }
    }

    try {
      const computedStyle = window.getComputedStyle(node);
      const backgroundColor = computedStyle.getPropertyValue('background-color');
      if (backgroundColor && backgroundColor !== 'transparent' && backgroundColor !== 'rgba(0, 0, 0, 0)') {
        return true;
      }
    } catch (e) {}

    return false;
  }

  function generateHashId(rect, content) {
    const str = `${content || ''}|${rect.left},${rect.top},${rect.width},${rect.height}`;
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32bit integer
    }
    return Math.abs(hash).toString(16);
  }

  function getPseudoElementContent(element, currentWindow) {
    if (!(element instanceof currentWindow.HTMLElement)) {
      return { before: '', after: '' };
    }
    try {
      const beforeContent = currentWindow.getComputedStyle(element, '::before').getPropertyValue('content');
      const afterContent = currentWindow.getComputedStyle(element, '::after').getPropertyValue('content');
      return {
        before: (!beforeContent || beforeContent === 'none') ? '' : beforeContent.replace(/"/g, ''),
        after: (!afterContent || afterContent === 'none') ? '' : afterContent.replace(/"/g, '')
      };
    } catch (e) {
      return { before: '', after: '' };
    }
  }

  function getRect(el, baseZoom, currentWindow) {
    let originalRect;
    let newZoom = 1;
    const hasGetBoundingClientRect = el instanceof Element;

    if (!hasGetBoundingClientRect) {
      const range = currentWindow.document.createRange();
      range.selectNodeContents(el);
      originalRect = range.getBoundingClientRect();
    } else {
      originalRect = el.getBoundingClientRect();
      if (el instanceof currentWindow.HTMLElement && !('currentCSSZoom' in el)) {
        try {
          newZoom = parseFloat(currentWindow.getComputedStyle(el).zoom) || 1;
        } catch (e) {}
      }
    }

    const zoom = newZoom * baseZoom;

    return {
      width: originalRect.width * zoom,
      height: originalRect.height * zoom,
      left: originalRect.left * zoom,
      top: originalRect.top * zoom,
      right: originalRect.right * zoom,
      bottom: originalRect.bottom * zoom,
      x: originalRect.x * zoom,
      y: originalRect.y * zoom,
      zoom
    };
  }

  function overlappedRect(rect1, rect2) {
    const left = Math.max(rect1.left, rect2.left);
    const top = Math.max(rect1.top, rect2.top);
    const right = Math.min(rect1.right, rect2.right);
    const bottom = Math.min(rect1.bottom, rect2.bottom);
    if (left < right && top < bottom) {
      return {
        left,
        top,
        right,
        bottom,
        width: right - left,
        height: bottom - top,
        x: left,
        y: top,
        zoom: 1
      };
    }
    return null;
  }

  function isElementPartiallyInViewport(rect, currentWindow, currentDocument) {
    const elementHeight = rect.height;
    const elementWidth = rect.width;

    const viewportRect = {
      left: 0,
      top: 0,
      width: currentWindow.innerWidth || currentDocument.documentElement.clientWidth,
      height: currentWindow.innerHeight || currentDocument.documentElement.clientHeight,
      right: currentWindow.innerWidth || currentDocument.documentElement.clientWidth,
      bottom: currentWindow.innerHeight || currentDocument.documentElement.clientHeight,
      x: 0,
      y: 0,
      zoom: 1
    };

    const overlapRect = overlappedRect(rect, viewportRect);
    if (!overlapRect) return false;

    const visibleArea = overlapRect.width * overlapRect.height;
    const totalArea = elementHeight * elementWidth;
    return visibleArea / totalArea >= 2 / 3;
  }

  function isElementCovered(el, rect, currentWindow) {
    const x = rect.left + rect.width / 2;
    const y = rect.top + rect.height / 2;

    try {
      const topElement = currentWindow.document.elementFromPoint(x, y);
      if (!topElement) return false;
      if (topElement === el || el.contains(topElement) || topElement.contains(el)) return false;

      const rectOfTopElement = getRect(topElement, 1, currentWindow);
      const overlapRect = overlappedRect(rect, rectOfTopElement);
      if (!overlapRect) return false;
      return true;
    } catch (e) {
      return false;
    }
  }

  function elementRect(el, currentWindow, currentDocument, baseZoom) {
    if (!el) return false;

    if (!(el instanceof currentWindow.HTMLElement) && el.nodeType !== Node.TEXT_NODE && el.nodeName.toLowerCase() !== 'svg') {
      return false;
    }

    if (el instanceof currentWindow.HTMLElement) {
      try {
        const style = currentWindow.getComputedStyle(el);
        if (style.display === 'none' || style.visibility === 'hidden' || (style.opacity === '0' && el.tagName !== 'INPUT')) {
          return false;
        }
      } catch (e) {}
    }

    const rect = getRect(el, baseZoom, currentWindow);
    if (rect.width === 0 && rect.height === 0) return false;

    if (baseZoom === 1 && isElementCovered(el, rect, currentWindow)) {
      return false;
    }

    const isVisible = isElementPartiallyInViewport(rect, currentWindow, currentDocument);

    // check if element is hidden by an ancestor's overflow:hidden
    let parent = el;
    while (parent && parent !== currentDocument.body) {
      if (!(parent instanceof currentWindow.HTMLElement)) {
        parent = parent.parentElement;
        continue;
      }
      try {
        const parentStyle = currentWindow.getComputedStyle(parent);
        if (parentStyle.overflow === 'hidden') {
          const parentRect = getRect(parent, 1, currentWindow);
          const tolerance = 10;
          if (
            rect.right < parentRect.left - tolerance ||
            rect.left > parentRect.right + tolerance ||
            rect.bottom < parentRect.top - tolerance ||
            rect.top > parentRect.bottom + tolerance
          ) {
            return false;
          }
        }
        if (parentStyle.position === 'fixed' || parentStyle.position === 'sticky') {
          break;
        }
      } catch (e) {}
      parent = parent.parentElement;
    }

    return {
      left: Math.round(rect.left),
      top: Math.round(rect.top),
      width: Math.round(rect.width),
      height: Math.round(rect.height),
      zoom: rect.zoom,
      isVisible
    };
  }

  function getNodeAttributes(node, currentWindow) {
    if (!node || !(node instanceof currentWindow.HTMLElement) || !node.attributes) {
      return {};
    }
    const attrs = {};
    for (let i = 0; i < node.attributes.length; i++) {
      const attr = node.attributes[i];
      let value = attr.value;
      if (attr.name === 'class') {
        value = '.' + value.split(' ').join('.');
      } else if (value.startsWith('data:image')) {
        value = 'image';
      }
      if (value.length > 300) {
        value = value.slice(0, 297) + '...';
      }
      attrs[attr.name] = value;
    }
    return attrs;
  }

  function mergeElementAndChildrenRects(node, currentWindow, currentDocument, baseZoom) {
    const selfRect = elementRect(node, currentWindow, currentDocument, baseZoom);
    if (!selfRect) return null;

    let minLeft = selfRect.left;
    let minTop = selfRect.top;
    let maxRight = selfRect.left + selfRect.width;
    let maxBottom = selfRect.top + selfRect.height;

    function traverse(child) {
      for (let i = 0; i < child.childNodes.length; i++) {
        const sub = child.childNodes[i];
        if (sub.nodeType === 1) {
          const rect = elementRect(sub, currentWindow, currentDocument, baseZoom);
          if (rect) {
            minLeft = Math.min(minLeft, rect.left);
            minTop = Math.min(minTop, rect.top);
            maxRight = Math.max(maxRight, rect.left + rect.width);
            maxBottom = Math.max(maxBottom, rect.top + rect.height);
          }
          traverse(sub);
        }
      }
    }
    traverse(node);

    return {
      ...selfRect,
      left: minLeft,
      top: minTop,
      width: maxRight - minLeft,
      height: maxBottom - minTop
    };
  }

  function collectElementInfo(node, currentWindow, currentDocument, baseZoom, basePoint, isContainer = false) {
    const rect = elementRect(node, currentWindow, currentDocument, baseZoom);
    if (!rect) return null;

    if (rect.width < CONTAINER_MINI_WIDTH || rect.height < CONTAINER_MINI_HEIGHT) {
      return null;
    }

    if (basePoint.left !== 0 || basePoint.top !== 0) {
      rect.left += basePoint.left;
      rect.top += basePoint.top;
    }

    // Skip full background elements
    if (rect.height >= currentWindow.innerHeight && rect.width >= currentWindow.innerWidth) {
      return null;
    }

    const midsceneId = idCounter++;
    if (node instanceof Element) {
      node.setAttribute('data-midscene-id', midsceneId);
    }

    // Form element processing
    if (isFormElement(node)) {
      const attributes = getNodeAttributes(node, currentWindow);
      let valueContent = attributes.value || attributes.placeholder || node.textContent || '';
      
      const tagName = node.tagName.toLowerCase();
      if (tagName === 'select') {
        const selectedOption = node.options[node.selectedIndex];
        valueContent = selectedOption ? (selectedOption.textContent || '') : '';
      } else if ((tagName === 'input' || tagName === 'textarea') && node.value) {
        valueContent = node.value;
      }

      const nodeHashId = generateHashId(rect, valueContent);
      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.FORM_ITEM,
        attributes: Object.assign({}, attributes, {
          htmlTagName: `<${tagName}>`,
          nodeType: NodeType.FORM_ITEM
        }),
        content: valueContent.trim(),
        rect,
        isVisible: rect.isVisible
      };
    }

    // Button element processing
    if (isButtonElement(node)) {
      const mergedRect = mergeElementAndChildrenRects(node, currentWindow, currentDocument, baseZoom) || rect;
      const attributes = getNodeAttributes(node, currentWindow);
      const pseudo = getPseudoElementContent(node, currentWindow);
      const content = node.innerText || pseudo.before || pseudo.after || '';
      const nodeHashId = generateHashId(mergedRect, content);
      const tagName = node.tagName.toLowerCase();

      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.BUTTON,
        attributes: Object.assign({}, attributes, {
          htmlTagName: `<${tagName}>`,
          nodeType: NodeType.BUTTON
        }),
        content: content.trim(),
        rect: mergedRect,
        isVisible: rect.isVisible
      };
    }

    // Image processing
    if (isImgElement(node)) {
      const attributes = getNodeAttributes(node, currentWindow);
      const nodeHashId = generateHashId(rect, '');
      const tagName = node.tagName.toLowerCase();

      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.IMG,
        attributes: Object.assign({}, attributes, {
          htmlTagName: `<${tagName}>`,
          nodeType: NodeType.IMG,
          svgContent: (tagName === 'svg' ? 'true' : undefined)
        }),
        content: '',
        rect,
        isVisible: rect.isVisible
      };
    }

    // Text processing
    if (isTextElement(node)) {
      const text = (node.textContent || '').trim().replace(/\n+/g, ' ');
      if (!text) return null;

      const attributes = getNodeAttributes(node, currentWindow);
      const nodeHashId = generateHashId(rect, text);
      const tagName = node.parentElement ? node.parentElement.tagName.toLowerCase() : '';

      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.TEXT,
        attributes: Object.assign({}, attributes, {
          htmlTagName: tagName ? `<${tagName}>` : '',
          nodeType: NodeType.TEXT
        }),
        content: text,
        rect,
        isVisible: rect.isVisible
      };
    }

    // Anchor processing
    if (isAElement(node)) {
      const attributes = getNodeAttributes(node, currentWindow);
      const pseudo = getPseudoElementContent(node, currentWindow);
      const content = node.innerText || pseudo.before || pseudo.after || '';
      const nodeHashId = generateHashId(rect, content);
      const tagName = node.tagName.toLowerCase();

      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.A,
        attributes: Object.assign({}, attributes, {
          htmlTagName: `<${tagName}>`,
          nodeType: NodeType.A
        }),
        content: content.trim(),
        rect,
        isVisible: rect.isVisible
      };
    }

    // Container processing
    if (isContainerElement(node) || isContainer) {
      const attributes = getNodeAttributes(node, currentWindow);
      const nodeHashId = generateHashId(rect, '');
      const tagName = node.tagName.toLowerCase();

      return {
        id: midsceneId,
        nodeHashId,
        nodeType: NodeType.CONTAINER,
        attributes: Object.assign({}, attributes, {
          htmlTagName: `<${tagName}>`,
          nodeType: NodeType.CONTAINER
        }),
        content: '',
        rect,
        isVisible: rect.isVisible
      };
    }

    return null;
  }

  function extractTreeNode(initNode) {
    idCounter = 1;
    const topDocument = document.body || document;
    const startNode = initNode || topDocument;
    const topChildren = [];

    function dfs(node, currentWindow, currentDocument, baseZoom, basePoint) {
      if (!node) return null;
      if (node.nodeType === 10) return null; // Doctype node

      const elementInfo = collectElementInfo(node, currentWindow, currentDocument, baseZoom, basePoint);

      const nodeInfo = {
        node: elementInfo,
        children: []
      };

      if (
        elementInfo && (
          elementInfo.nodeType === NodeType.BUTTON ||
          elementInfo.nodeType === NodeType.IMG ||
          elementInfo.nodeType === NodeType.TEXT ||
          elementInfo.nodeType === NodeType.FORM_ITEM ||
          elementInfo.nodeType === NodeType.A ||
          elementInfo.nodeType === NodeType.CONTAINER
        )
      ) {
        return nodeInfo;
      }

      const rect = getRect(node, baseZoom, currentWindow);
      for (let i = 0; i < node.childNodes.length; i++) {
        const childNodeInfo = dfs(node.childNodes[i], currentWindow, currentDocument, rect.zoom, basePoint);
        if (Array.isArray(childNodeInfo)) {
          nodeInfo.children.push(...childNodeInfo);
        } else if (childNodeInfo) {
          nodeInfo.children.push(childNodeInfo);
        }
      }

      if (nodeInfo.node === null) {
        if (nodeInfo.children.length === 0) return null;
        return nodeInfo.children;
      }

      return nodeInfo;
    }

    const rootNodeInfo = dfs(startNode, window, document, 1, { left: 0, top: 0 });
    if (Array.isArray(rootNodeInfo)) {
      topChildren.push(...rootNodeInfo);
    } else if (rootNodeInfo) {
      topChildren.push(rootNodeInfo);
    }

    if (startNode === topDocument) {
      const iframes = document.querySelectorAll('iframe');
      for (let i = 0; i < iframes.length; i++) {
        const iframe = iframes[i];
        if (iframe.contentDocument && iframe.contentWindow) {
          const iframeInfo = collectElementInfo(iframe, window, document, 1, { left: 0, top: 0 });
          if (iframeInfo) {
            const iframeChildren = dfs(
              iframe.contentDocument.body,
              iframe.contentWindow,
              iframe.contentDocument,
              1,
              { left: iframeInfo.rect.left, top: iframeInfo.rect.top }
            );
            if (Array.isArray(iframeChildren)) {
              topChildren.push(...iframeChildren);
            } else if (iframeChildren) {
              topChildren.push(iframeChildren);
            }
          }
        }
      }
    }

    return {
      node: null,
      children: topChildren
    };
  }

  function truncateText(text, maxLength = 150) {
    if (text === undefined || text === null) return '';
    if (typeof text === 'object') text = JSON.stringify(text);
    if (typeof text === 'number') return text.toString();
    if (typeof text === 'string' && text.length > maxLength) {
      return text.slice(0, maxLength) + '...';
    }
    return text.trim();
  }

  function trimAttributes(attributes, truncateTextLength) {
    const res = {};
    for (const [key, value] of Object.entries(attributes)) {
      if (key === 'style' || key === 'htmlTagName' || key === 'nodeType') {
        continue;
      }
      res[key] = truncateText(value, truncateTextLength);
    }
    return res;
  }

  function escapeXml(unsafe) {
    if (unsafe === null || unsafe === undefined) return '';
    if (typeof unsafe !== 'string') unsafe = String(unsafe);
    return unsafe.replace(/[<>&"']/g, function (m) {
      switch (m) {
        case '<': return '&lt;';
        case '>': return '&gt;';
        case '&': return '&amp;';
        case '"': return '&quot;';
        case "'": return '&apos;';
        default: return m;
      }
    });
  }

  function descriptionOfTree(tree, truncateTextLength, filterNonTextContent = false, visibleOnly = true) {
    const attributesString = (kv) => {
      return Object.entries(kv)
        .map(([key, value]) => `${key}="${escapeXml(truncateText(value, truncateTextLength))}"`)
        .join(' ');
    };

    function buildContentTree(node, indent = 0) {
      let before = '';
      let contentWithIndent = '';
      let after = '';
      let emptyNode = true;
      const indentStr = '  '.repeat(indent);

      let children = '';
      for (let i = 0; i < (node.children || []).length; i++) {
        const childContent = buildContentTree(node.children[i], indent + 1);
        if (childContent) {
          children += `\n${childContent}`;
        }
      }

      if (
        node.node &&
        node.node.rect.width > nodeSizeThreshold &&
        node.node.rect.height > nodeSizeThreshold &&
        (!filterNonTextContent || (filterNonTextContent && node.node.content)) &&
        (!visibleOnly || (visibleOnly && node.node.isVisible))
      ) {
        emptyNode = false;
        let nodeTypeString;
        if (node.node.attributes && node.node.attributes.htmlTagName) {
          nodeTypeString = node.node.attributes.htmlTagName.replace(/[<>]/g, '');
        } else {
          nodeTypeString = node.node.nodeType.toLowerCase();
        }

        const rectAttribute = {
          rect: `${node.node.rect.left},${node.node.rect.top},${node.node.rect.width},${node.node.rect.height}`
        };

        const trimmedAttrs = trimAttributes(node.node.attributes || {}, truncateTextLength);
        const attrsStr = attributesString(trimmedAttrs);
        const rectStr = attributesString(rectAttribute);

        before = `<${nodeTypeString} id="${node.node.id}" hash="${node.node.nodeHashId}" ${attrsStr} ${rectStr}>`;
        const content = truncateText(node.node.content, truncateTextLength);
        contentWithIndent = content ? `${escapeXml(content)}` : '';
        after = `</${nodeTypeString}>`;
      }

      if (emptyNode && !children.trim()) {
        return '';
      }

      if (emptyNode) {
        return children.trim();
      }

      return `${indentStr}${before}${contentWithIndent}${children}${children ? '\n' + indentStr : ''}${after}`;
    }

    const result = buildContentTree(tree, 1);
    return `<page>\n${result}\n</page>`;
  }

  try {
    const tree = extractTreeNode(document.body);
    return descriptionOfTree(tree, 200, false, true);
  } catch (e) {
    return '<error>' + escapeXml(e.message) + '</error>';
  }
})();