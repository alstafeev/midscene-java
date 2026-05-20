(function() {
  const nodeSizeThreshold = 4;
  let idCounter = 1;

  function isFormElement(node) {
    const tag = node.tagName.toLowerCase();
    return ['input', 'textarea', 'select', 'option'].includes(tag);
  }

  function isButtonElement(node) {
    const tag = node.tagName.toLowerCase();
    const role = node.getAttribute('role');
    return tag === 'button' || role === 'button';
  }

  function isAElement(node) {
    return node.tagName.toLowerCase() === 'a';
  }

  function isImgElement(node) {
    const tag = node.tagName.toLowerCase();
    if (tag === 'img' || tag === 'svg') return true;
    
    // Background image check
    try {
      const style = window.getComputedStyle(node);
      return style.getPropertyValue('background-image') !== 'none';
    } catch (e) {
      return false;
    }
  }

  function generateHash(tag, text, rect, attrs) {
    const str = `${tag}|${text}|${rect.left},${rect.top},${rect.width},${rect.height}|${JSON.stringify(attrs)}`;
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32bit integer
    }
    return Math.abs(hash).toString(16);
  }

  function getVisibleElements(root, baseOffset = { left: 0, top: 0 }) {
    const elements = [];

    function processNode(node, currentOffset) {
      // 1. Handle Text Nodes (only standalone ones)
      if (node.nodeType === Node.TEXT_NODE) {
        // We handle text nodes within elements mostly, 
        // but standalone text nodes can occur in Shadow DOM or other roots.
        const text = node.textContent.trim();
        if (text && node.parentElement && !isInteractiveElement(node.parentElement)) {
            const range = document.createRange();
            range.selectNodeContents(node);
            const rects = range.getClientRects();
            if (rects.length > 0) {
              const rect = rects[0];
              if (rect.width > nodeSizeThreshold && rect.height > nodeSizeThreshold) {
                const elRect = {
                  left: Math.round(rect.left + currentOffset.left),
                  top: Math.round(rect.top + currentOffset.top),
                  width: Math.round(rect.width),
                  height: Math.round(rect.height)
                };
                elements.push({
                  type: 'text',
                  text: text,
                  rect: elRect,
                  hash: generateHash('text', text, elRect, {})
                });
              }
            }
        }
        return;
      }

      // 2. Handle Element Nodes
      if (node.nodeType !== Node.ELEMENT_NODE) return;

      const tag = node.tagName.toLowerCase();
      if (['script', 'style', 'noscript', 'meta', 'head', 'link', 'base'].includes(tag)) return;

      let style;
      try {
        style = window.getComputedStyle(node);
        if (style.display === 'none' || style.visibility === 'hidden' || parseFloat(style.opacity) === 0) return;
      } catch (e) {}

      const rect = node.getBoundingClientRect();
      if (rect.width < nodeSizeThreshold || rect.height < nodeSizeThreshold) return;

      const midsceneId = idCounter++;
      node.setAttribute('data-midscene-id', midsceneId);

      const elRect = {
        left: Math.round(rect.left + currentOffset.left),
        top: Math.round(rect.top + currentOffset.top),
        width: Math.round(rect.width),
        height: Math.round(rect.height)
      };

      const elData = {
        id: midsceneId,
        tag: tag,
        rect: elRect,
        attributes: {}
      };

      const importantAttrs = ['id', 'class', 'placeholder', 'alt', 'type', 'aria-label', 'role', 'name', 'title', 'value'];
      importantAttrs.forEach(attr => {
        const val = node.getAttribute(attr);
        if (val) {
            let valStr = String(val);
            if (valStr.length > 50) valStr = valStr.substring(0, 47) + '...';
            elData.attributes[attr] = valStr;
        }
      });

      if (isButtonElement(node)) elData.type = 'button';
      else if (isFormElement(node)) elData.type = 'form_item';
      else if (isAElement(node)) elData.type = 'a';
      else if (isImgElement(node)) elData.type = 'img';
      else elData.type = 'container';

      let directText = '';
      for (let i = 0; i < node.childNodes.length; i++) {
        const child = node.childNodes[i];
        if (child.nodeType === Node.TEXT_NODE) {
          directText += child.textContent.trim() + ' ';
        }
      }
      directText = directText.trim();
      if (directText) elData.text = directText;

      elData.hash = generateHash(tag, elData.text || '', elRect, elData.attributes);

      if (isInteractiveElement(node) || elData.text || elData.attributes['aria-label'] || elData.attributes['alt'] || tag === 'img') {
        elements.push(elData);
      }

      if (node.shadowRoot) {
        elements.push(...getVisibleElements(node.shadowRoot, currentOffset));
      }

      if (tag === 'iframe') {
        try {
          if (node.contentDocument && node.contentDocument.body) {
            const iframeOffset = {
              left: currentOffset.left + rect.left,
              top: currentOffset.top + rect.top
            };
            elements.push(...getVisibleElements(node.contentDocument.body, iframeOffset));
          }
        } catch (e) {}
      }

      for (let i = 0; i < node.children.length; i++) {
        processNode(node.children[i], currentOffset);
      }
    }

    function isInteractiveElement(node) {
        const tag = node.tagName.toLowerCase();
        return ['a', 'button', 'input', 'select', 'textarea'].includes(tag) || 
               node.hasAttribute('onclick') || 
               (window.getComputedStyle(node).cursor === 'pointer');
    }

    processNode(root, baseOffset);
    return elements;
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

  function toXml(elements) {
    let xml = '<page>
';
    elements.forEach(el => {
      const tag = el.type === 'text' ? 'text' : el.tag;
      xml += `  <${tag}`;
      if (el.id) xml += ` id="${el.id}"`;
      if (el.hash) xml += ` hash="${el.hash}"`;
      if (el.rect) {
        xml += ` rect="${el.rect.left},${el.rect.top},${el.rect.width},${el.rect.height}"`;
      }
      if (el.attributes) {
        for (const [key, value] of Object.entries(el.attributes)) {
          xml += ` ${key}="${escapeXml(value)}"`;
        }
      }
      xml += '>';
      if (el.text) {
        let txt = el.text;
        if (txt.length > 100) txt = txt.substring(0, 97) + '...';
        xml += escapeXml(txt);
      }
      xml += `</${tag}>
`;
    });
    xml += '</page>';
    return xml;
  }

  try {
    const allElements = getVisibleElements(document.body);
    return toXml(allElements);
  } catch (e) {
    return '<error>' + escapeXml(e.message) + '</error>';
  }
})();