package com.temenos.interaction.core.hypermedia;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A Link (see http://tools.ietf.org/html/rfc5988)
 * @author aphethean
 */
public class Link {

    private Transition transition;

    // id
    private String id;
    // title (5.4 Target Attributes)
    private String title;
    // rel (5.3 Relation Type)
    private String rel;
    // href (5.1 Target IRI)
    private String href;
    // type (5.4 Target Attributes)
    private String[] produces;

    // extensions
    private String method;
    private String[] consumes;
    private MultivaluedMap<String, String> extensions;
    
    //LinkId
    private String linkId;
    
    //Source Field
    private String sourceField;

    /**
     * Construct a simple link used for GET operations.
     * @param title
     * @param rel
     * @param href
     * @param type
     * @param extensions
     */
    public Link(String title, String rel, String href, String type,
            MultivaluedMap<String, String> extensions) {
        this(null, title, rel, href, null, (type != null ? type.split(" ")
                : null), HttpMethod.GET, null);
    }

    /**
     * Construct a link from a transition.
     * @param transition
     */
    public Link(Transition transition, String rel, String href, String method) {
        this(transition, transition.getLabel() != null
                && !transition.getLabel().equals("") ? transition.getLabel()
                : transition.getTarget().getName(), rel, href, null, null,
                method, null);
    }
    
    /**
     * Construct a link from a transition having a uri with a multivalue drill-down
     * @param transition
     */
    public Link(Transition transition, String rel, String href, String method, String sourceField) {
        this(transition, transition.getLabel() != null
                && !transition.getLabel().equals("") ? transition.getLabel()
                : transition.getTarget().getName(), rel, href, null, null,
                method, null, sourceField);
    }

    public Link(Transition transition, String title, String rel, String href,
            String[] consumes, String[] produces, String method,
            MultivaluedMap<String, String> extensions) {        
        this(transition, title, rel, href, consumes, produces, method, extensions, null);
    }
    
    public Link(Transition transition, String title, String rel, String href,
            String[] consumes, String[] produces, String method,
            MultivaluedMap<String, String> extensions, String sourceField) {
        this.transition = transition;
        if (title == null && transition != null) {
            title = transition.getId();
        }
        id = transition != null ? transition.getId() : title;
        this.title = title;
        this.rel = rel;
        this.href = href;
        this.consumes = consumes;
        this.produces = produces;
        this.method = method;
        this.extensions = extensions;
        this.linkId = transition != null ? transition.getLinkId() : null;
        this.sourceField = sourceField;
    }

    public Transition getTransition() {
        return transition;
    }

    public String getId() {
        return id;
    }

    public String getRel() {
        return rel;
    }

    public String getMethod() {
        return method;
    }

    public String getHref() {
        return href;
    }
    
    public String getSourceField() {
        return sourceField;
    }

    public String getLinkId() {
        if(linkId == null) {
            linkId = transition != null ? transition.getLinkId() : null;    
        } 
        return linkId;
    }
    /**
     * Obtain the transition, i.e. the link relative to the REST service.
     * {@link https://www.ietf.org/rfc/rfc1738.txt}
     * 
     * @param basePath  Path to REST service
     * @return relativePath of transition relative to REST service 
     */
    public String getRelativeHref(String basePath) {
        // Trimming standard port number.
        String baseUri = HypermediaTemplateHelper
                .getTemplatedBaseUri(basePath.replaceFirst(":80/", "/").replaceFirst(":443/", "/"), href);
        StringBuffer regex = new StringBuffer("(?<=" + baseUri + ")\\S+");
        Pattern p = Pattern.compile(regex.toString());
        Matcher m = p.matcher(href);
        while (m.find()) {
            return m.group();
        }
        return href;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public String getTitle() {
        return title;
    }

    public MultivaluedMap<String, String> getExtensions() {
        return extensions;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("<");
        buf.append(href).append(">");
        if (rel != null) {
            buf.append("; rel=\"").append(rel).append("\"");
        }
        if (produces != null && produces.length > 0) {
            buf.append("; type=\"").append(produces[0]);
            for (int i = 1; i < produces.length; i++)
                buf.append(",").append(produces[i]);
            buf.append("\"");
        }
        if (title != null) {
            buf.append("; title=\"").append(title).append("\"");
        }
        if (extensions != null) {
            for (String key : getExtensions().keySet()) {
                List<String> values = getExtensions().get(key);
                for (String val : values) {
                    buf.append("; ").append(key).append("=\"").append(val)
                            .append("\"");
                }
            }
        }
        
        if (linkId != null) {
            buf.append("; linkId=\"").append(linkId).append("\"");
        }
        
        if (sourceField != null) {
            buf.append("; sourceField=\"").append(sourceField).append("\"");
        }
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Link) {
            Link otherLink = (Link) other;
            return this.toString().equals(otherLink.toString());
        }
        return false;
    }
    
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    public static class Builder {
        private Transition transition;
        private String id;
        private String title;
        private String rel;
        private String href;
        private String[] produces;
        private String method;
        private String[] consumes;
        private MultivaluedMap<String, String> extensions;
        private String linkId;
        private String sourceField;

        public Builder transition(Transition transition) {
            this.transition = transition;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder rel(String rel) {
            this.rel = rel;
            return this;
        }

        public Builder href(String href) {
            this.href = href;
            return this;
        }

        public Builder produces(String[] produces) {
            this.produces = produces;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder consumes(String[] consumes) {
            this.consumes = consumes;
            return this;
        }

        public Builder extensions(MultivaluedMap<String, String> extensions) {
            this.extensions = extensions;
            return this;
        }

        public Builder linkId(String linkId) {
            this.linkId = linkId;
            return this;
        }
        
        public Builder sourceField(String sourceField) {
            this.sourceField = sourceField;
            return this;
        }

        public Link build() {
            return new Link(this);
        }
    }

    private Link(Builder builder) {
        this.transition = builder.transition;
        this.id = builder.id;
        this.title = builder.title;
        this.rel = builder.rel;
        this.href = builder.href;
        this.produces = builder.produces;
        this.method = builder.method;
        this.consumes = builder.consumes;
        this.extensions = builder.extensions;
        if(builder.linkId == null) {
            this.linkId = builder.transition != null ? builder.transition.getLinkId() : null;   
        } else {
            this.linkId = builder.linkId;
        }
        this.sourceField = builder.sourceField;
    }
}
