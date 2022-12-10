FROM node:18-alpine3.15 as build
WORKDIR /app
RUN npm install -g @angular/cli
COPY ./package.json .
RUN npm config set legacy-peer-deps true
RUN npm install
COPY . .
RUN ng build
FROM nginx as runtime
COPY --from=build /app/dist/simple-bills-gui /usr/share/nginx/html
