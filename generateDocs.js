const fs = require('fs');

function generateMarkdown() {
  let rawData = fs.readFileSync('swagger.json', 'utf8');
  if (rawData.charCodeAt(0) === 0xFEFF) {
    rawData = rawData.slice(1);
  }
  const data = JSON.parse(rawData);
  let md = '# Complete API Documentation\n\n';
  
  const paths = data.paths;
  const components = data.components || {};
  const schemas = components.schemas || {};
  
  function resolveRef(ref) {
    if (!ref) return null;
    const parts = ref.split('/');
    const name = parts[parts.length - 1];
    return schemas[name];
  }
  
  function formatSchema(schema, indent = '') {
    if (!schema) return 'Any';
    if (schema.$ref) {
      const resolved = resolveRef(schema.$ref);
      if (resolved) return formatSchema(resolved, indent);
      return schema.$ref.split('/').pop();
    }
    
    if (schema.type === 'array') {
      return `Array<${formatSchema(schema.items, indent)}>\n`;
    }
    
    if (schema.type === 'object' || schema.properties) {
      let str = '{\n';
      for (const [key, prop] of Object.entries(schema.properties || {})) {
        str += `${indent}  "${key}": ${formatSchema(prop, indent + '  ')},\n`;
      }
      str += `${indent}}`;
      return str;
    }
    
    if (schema.enum) {
      return `Enum(${schema.enum.join(' | ')})`;
    }
    
    return schema.type || 'unknown';
  }

  for (const [path, methods] of Object.entries(paths)) {
    for (const [method, operation] of Object.entries(methods)) {
      md += `## ${method.toUpperCase()} ${path}\n\n`;
      
      if (operation.summary) md += `**Summary:** ${operation.summary}\n\n`;
      if (operation.description) md += `**Description:** ${operation.description}\n\n`;
      
      // Parameters
      if (operation.parameters && operation.parameters.length > 0) {
        md += `### Parameters\n\n`;
        md += `| Name | In | Required | Type | Description |\n`;
        md += `|---|---|---|---|---|\n`;
        for (const p of operation.parameters) {
          const pType = p.schema ? (p.schema.type || 'unknown') : 'unknown';
          md += `| \`${p.name}\` | \`${p.in}\` | ${p.required ? 'Yes' : 'No'} | \`${pType}\` | ${p.description || ''} |\n`;
        }
        md += `\n`;
      }
      
      // Request Body
      if (operation.requestBody && operation.requestBody.content) {
        md += `### Request Body\n\n`;
        const content = operation.requestBody.content['application/json'];
        if (content && content.schema) {
          md += '```json\n';
          md += formatSchema(content.schema);
          md += '\n```\n\n';
        } else {
          md += `See content types: ${Object.keys(operation.requestBody.content).join(', ')}\n\n`;
        }
      }
      
      // Responses
      if (operation.responses) {
        md += `### Responses\n\n`;
        for (const [code, response] of Object.entries(operation.responses)) {
          md += `#### ${code}\n\n`;
          if (response.description) md += `**Description:** ${response.description}\n\n`;
          if (response.content && response.content['application/json']) {
             const schema = response.content['application/json'].schema;
             if (schema) {
               md += '```json\n';
               md += formatSchema(schema);
               md += '\n```\n\n';
             }
          } else if (response.content && response.content['*/*']) {
             const schema = response.content['*/*'].schema;
             if (schema) {
               md += '```json\n';
               md += formatSchema(schema);
               md += '\n```\n\n';
             }
          }
        }
      }
      
      md += '---\n\n';
    }
  }
  
  fs.writeFileSync('docs/FRONTEND_API_DOCS.md', md);
  console.log('API Docs generated successfully at docs/FRONTEND_API_DOCS.md');
}

generateMarkdown();
