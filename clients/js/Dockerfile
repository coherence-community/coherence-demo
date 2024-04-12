FROM node:18-alpine
# Create app directory
WORKDIR /usr/src/app
# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
COPY package*.json ./
COPY main.js ./

RUN npm install

CMD [ "node", "main.js" ]