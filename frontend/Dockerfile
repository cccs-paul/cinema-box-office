# Single stage - use pre-built Angular app (run `npm run build` before docker build)
# This avoids SSL issues in corporate networks with proxy/firewall interception
FROM nginx:stable

# Remove default nginx static content
RUN rm -rf /usr/share/nginx/html/*

# Copy pre-built Angular app (build with: cd frontend && npm run build)
# Angular 17+ outputs to browser subfolder
COPY frontend/dist/myrc/browser /usr/share/nginx/html

# Copy custom Nginx configuration
COPY frontend/nginx.conf /etc/nginx/nginx.conf

# Create required directories and set permissions for non-root user
RUN mkdir -p /tmp/nginx/body /tmp/nginx/proxy /tmp/nginx/fastcgi && \
    chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /tmp/nginx && \
    touch /var/run/nginx.pid && \
    chown nginx:nginx /var/run/nginx.pid

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -sf http://localhost:80/ || exit 1

# Expose port
EXPOSE 80

# Switch to non-root user (nginx user exists in nginx image)
USER nginx

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
